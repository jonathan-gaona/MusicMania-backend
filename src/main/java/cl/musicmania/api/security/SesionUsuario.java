package cl.musicmania.api.security;

/** Datos del usuario autenticado, extraídos del JWT (disponible con @AuthenticationPrincipal). */
public record SesionUsuario(Long idUsuario, String rol) {
}
