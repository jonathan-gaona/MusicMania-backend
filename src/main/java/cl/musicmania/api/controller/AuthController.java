package cl.musicmania.api.controller;

import cl.musicmania.api.dto.Dtos.*;
import cl.musicmania.api.security.SesionUsuario;
import cl.musicmania.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login, renovación de token y sesión actual")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un cliente nuevo (público)")
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve access token + refresh token (público)")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva el access token usando el refresh token (público)")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return service.renovar(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Devuelve el usuario dueño del token (para revalidar la sesión al recargar)")
    public UsuarioDto me(@AuthenticationPrincipal SesionUsuario sesion) {
        return service.me(sesion.idUsuario());
    }
}
