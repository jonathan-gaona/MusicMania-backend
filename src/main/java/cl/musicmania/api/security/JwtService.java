package cl.musicmania.api.security;

import cl.musicmania.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los tokens JWT (firmados con HS256).
 *  - access token:  corta duración, se envía en cada petición (Authorization: Bearer ...)
 *  - refresh token: larga duración, solo sirve para pedir un access token nuevo (/api/auth/refresh)
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-minutes}") long accessMinutes,
                      @Value("${app.jwt.refresh-days}") long refreshDays) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessMs = accessMinutes * 60_000L;
        this.refreshMs = refreshDays * 24L * 60L * 60_000L;
    }

    public String generarAccessToken(Usuario u) {
        return construir(u, TIPO_ACCESS, accessMs);
    }

    public String generarRefreshToken(Usuario u) {
        return construir(u, TIPO_REFRESH, refreshMs);
    }

    private String construir(Usuario u, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("rol", u.getRol())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(key)
                .compact();
    }

    /** Verifica la firma y la expiración. Lanza JwtException si el token no es válido. */
    public Claims leer(String token) throws JwtException {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
