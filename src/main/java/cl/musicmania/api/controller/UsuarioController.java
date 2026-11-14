package cl.musicmania.api.controller;

import cl.musicmania.api.dto.Dtos.MensajeDto;
import cl.musicmania.api.dto.Dtos.UsuarioDto;
import cl.musicmania.api.dto.Dtos.UsuarioRequest;
import cl.musicmania.api.security.SesionUsuario;
import cl.musicmania.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Mantenedor de usuarios (solo ADMINISTRADOR)")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public List<UsuarioDto> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con el rol indicado")
    public UsuarioDto crear(@RequestBody UsuarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario (contraseña vacía = no se cambia)")
    public UsuarioDto actualizar(@PathVariable Long id, @RequestBody UsuarioRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un usuario (no permite borrar al admin principal ni a uno mismo)")
    public MensajeDto eliminar(@PathVariable Long id, @AuthenticationPrincipal SesionUsuario sesion) {
        return service.eliminar(id, sesion);
    }
}
