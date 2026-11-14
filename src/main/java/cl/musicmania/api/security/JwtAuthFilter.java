package cl.musicmania.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta en cada petición: si trae "Authorization: Bearer <token>" válido, deja al usuario autenticado
 * con su rol. Si no hay token (o es inválido), la petición sigue como anónima y SecurityConfig decide
 * si el recurso es público o requiere autenticación.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.leer(header.substring(7));
                // Un refresh token NO sirve para acceder a recursos
                if (JwtService.TIPO_ACCESS.equals(claims.get("tipo", String.class))) {
                    String rol = claims.get("rol", String.class);
                    SesionUsuario sesion = new SesionUsuario(Long.valueOf(claims.getSubject()), rol);
                    var auth = new UsernamePasswordAuthenticationToken(
                            sesion, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token vencido o manipulado: se ignora y la petición queda sin autenticar
            }
        }
        chain.doFilter(request, response);
    }
}
