package cl.musicmania.api.controller;

import cl.musicmania.api.dto.Dtos.*;
import cl.musicmania.api.security.SesionUsuario;
import cl.musicmania.api.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Checkout e historial de compras")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Checkout: crea la orden y descuenta stock (público: acepta invitados)")
    public OrdenDto crear(@RequestBody OrdenRequest request, @AuthenticationPrincipal SesionUsuario sesion) {
        return service.crear(request, sesion);
    }

    @GetMapping("/mias")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Historial de compras del usuario autenticado")
    public List<OrdenDto> mias(@AuthenticationPrincipal SesionUsuario sesion) {
        return service.misOrdenes(sesion.idUsuario());
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista todas las órdenes (solo ADMINISTRADOR)")
    public List<OrdenDto> listar() {
        return service.listarTodas();
    }

    @PatchMapping("/{id}/estado")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambia el estado de una orden (solo ADMINISTRADOR)")
    public MensajeDto cambiarEstado(@PathVariable String id, @Valid @RequestBody EstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Elimina una orden (solo ADMINISTRADOR)")
    public MensajeDto eliminar(@PathVariable String id) {
        return service.eliminar(id);
    }
}
