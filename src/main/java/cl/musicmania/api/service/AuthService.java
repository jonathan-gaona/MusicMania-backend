package cl.musicmania.api.service;

import cl.musicmania.api.dto.Dtos.*;
import cl.musicmania.api.dto.Mapeos;
import cl.musicmania.api.exception.ApiException;
import cl.musicmania.api.model.Usuario;
import cl.musicmania.api.repository.UsuarioRepository;
import cl.musicmania.api.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern CORREO = Pattern.compile("^[\\w.-]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
            Pattern.CASE_INSENSITIVE);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final GoogleAuthService googleAuth;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt,
            GoogleAuthService googleAuth) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.googleAuth = googleAuth;
    }

    @Transactional
    public RegistroResponse registrar(RegistroRequest r) {
        if (!CORREO.matcher(r.correo().trim()).matches())
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com.");
        if (r.password().length() < 4 || r.password().length() > 10)
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 4 y 10 caracteres.");

        String correo = r.correo().trim().toLowerCase();
        String run = (r.run() == null || r.run().isBlank()) ? null : r.run().trim();

        if (usuarios.existsByCorreoIgnoreCase(correo) || (run != null && usuarios.existsByRun(run)))
            throw new ApiException(HttpStatus.CONFLICT, "El correo o RUN ya se encuentra registrado.");

        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(r.nombre().trim());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRegion(r.region());
        u.setComuna(r.comuna());
        u.setDireccion(r.direccion() == null ? null : r.direccion().trim());
        u.setRol("Cliente");
        usuarios.save(u);
        return new RegistroResponse(u.getId(), "Usuario creado correctamente");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        Usuario u = usuarios.findByCorreoIgnoreCase(r.correo().trim())
                .filter(x -> x.getPasswordHash() != null && encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        return emitir(u);
    }

    @Transactional
    public AuthResponse loginConGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El token de Google es obligatorio.");
        }

        GoogleIdToken.Payload payload;
        try {
            payload = googleAuth.verificarToken(idToken);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de Google inválido o expirado.");
        }

        String correo = payload.getEmail().trim().toLowerCase();
        String nombreGoogle = (String) payload.get("name");
        String nombre = (nombreGoogle != null && !nombreGoogle.isBlank()) ? nombreGoogle.trim() : correo.split("@")[0];

        Usuario u = usuarios.findByCorreoIgnoreCase(correo).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setCorreo(correo);
            nuevo.setRol("Cliente");
            nuevo.setPasswordHash(encoder.encode(UUID.randomUUID().toString()));
            return usuarios.save(nuevo);
        });

        return emitir(u);
    }

    @Transactional(readOnly = true)
    public AuthResponse renovar(RefreshRequest r) {
        try {
            Claims claims = jwt.leer(r.refreshToken());
            if (!JwtService.TIPO_REFRESH.equals(claims.get("tipo", String.class)))
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de renovación inválido.");
            Usuario u = usuarios.findById(Long.valueOf(claims.getSubject()))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado."));
            return emitir(u);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de renovación inválido o expirado.");
        }
    }

    @Transactional(readOnly = true)
    public UsuarioDto me(Long idUsuario) {
        return usuarios.findById(idUsuario).map(Mapeos::usuario)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado."));
    }

    private AuthResponse emitir(Usuario u) {
        return new AuthResponse(jwt.generarAccessToken(u), jwt.generarRefreshToken(u), Mapeos.usuario(u));
    }
}