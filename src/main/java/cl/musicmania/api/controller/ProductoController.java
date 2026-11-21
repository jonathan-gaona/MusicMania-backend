package cl.musicmania.api.controller;

import cl.musicmania.api.dto.Dtos.MensajeDto;
import cl.musicmania.api.dto.Dtos.ProductoDto;
import cl.musicmania.api.dto.Dtos.ProductoRequest;
import cl.musicmania.api.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de vinilos, equipos y accesorios")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista productos (público). Filtro opcional ?categoria=vinilos|equipos|accesorios|ofertas")
    public List<ProductoDto> listar(@RequestParam(required = false) String categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto (público)")
    public ProductoDto obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crea un producto (solo ADMINISTRADOR)")
    public ProductoDto crear(@RequestBody ProductoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualiza un producto, solo los campos enviados (solo ADMINISTRADOR)")
    public ProductoDto actualizar(@PathVariable String id, @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Elimina un producto (solo ADMINISTRADOR)")
    public MensajeDto eliminar(@PathVariable String id) {
        return service.eliminar(id);
    }
}
