package cl.musicmania.api.controller;

import cl.musicmania.api.dto.Dtos.ResumenAdminDto;
import cl.musicmania.api.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administración", description = "Resumen del panel de administración (solo ADMINISTRADOR)")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Métricas reales del negocio: usuarios, productos, ventas y stock bajo")
    public ResumenAdminDto resumen() {
        return service.resumen();
    }
}
