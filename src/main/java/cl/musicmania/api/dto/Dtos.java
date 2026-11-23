package cl.musicmania.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/** Objetos de entrada/salida de la API (lo que viaja en el JSON). */
public final class Dtos {
    private Dtos() {}

    public record MensajeDto(String message) {}

    // ---------- Autenticación ----------
    public record LoginRequest(
            @NotBlank(message = "Correo y contraseña son obligatorios.") String correo,
            @NotBlank(message = "Correo y contraseña son obligatorios.") String password) {}

    public record RegistroRequest(
            String run,
            @NotBlank(message = "El nombre es obligatorio.") String nombre,
            @NotBlank(message = "El correo es obligatorio.") String correo,
            @NotBlank(message = "La contraseña es obligatoria.") String password,
            String region, String comuna, String direccion) {}

    public record RegistroResponse(Long id, String message) {}

    public record RefreshRequest(@NotBlank(message = "Falta el token de renovación.") String refreshToken) {}

    public record UsuarioDto(Long id, String run, String nombre, String correo,
                             String region, String comuna, String direccion, String rol) {}

    /** token = access token (corta duración); refreshToken = para renovarlo sin volver a iniciar sesión. */
    public record AuthResponse(String token, String refreshToken, UsuarioDto usuario) {}

    // ---------- Usuarios (admin) ----------
    public record UsuarioRequest(String nombre, String run, String correo, String password, String rol) {}

    // ---------- Productos ----------
    public record ProductoDto(String id, String categoria, String titulo, String nombre, String artista,
                              String descripcion, String sku, Integer precio, Integer precioAntes,
                              Integer stock, String imagen, Integer anio, String spotifyUrl) {}

    public record ProductoRequest(String titulo, String artista, String categoria, Integer precio,
                                  Integer stock, String imagen, String descripcion, String sku,
                                  Integer anio, String spotifyUrl, Integer precioAntes) {}

    // ---------- Órdenes ----------
    public record ClienteDto(String nombre, String apellidos, String correo, String calle,
                             String depto, String region, String comuna, String indicaciones) {}

    public record ItemDto(String id, Integer cantidad) {}

    public record OrdenRequest(ClienteDto cliente, List<ItemDto> items, String metodoPago) {}

    public record EstadoRequest(@NotBlank(message = "El estado es obligatorio.") String estado) {}

    public record LineaDto(String id, String nombre, Integer precio, Integer cantidad) {}

    /** idUsuario es null cuando la compra fue como invitado. */
    public record OrdenDto(String id, String nroOrden, String fecha, String cliente, String correo,
                           String rut, String direccion, String metodoPago, String estado,
                           Integer total, List<LineaDto> productos, Long idUsuario) {}

    // ---------- Resumen del panel de administración ----------
    public record ProductoVendidoDto(String id, String nombre, long unidades, long ingresos) {}

    public record ProductoStockBajoDto(String id, String titulo, String categoria, int stock) {}

    public record ResumenAdminDto(
            // Usuarios
            int totalUsuarios, int clientes, int administradores, int vendedores,
            // Productos
            int totalProductos, long unidadesEnStock, int productosStockBajo,
            // Órdenes (las canceladas no suman a las ventas)
            int totalOrdenes, long ventasTotales, long ticketPromedio,
            Map<String, Integer> ordenesPorEstado,
            Map<String, Long> ventasPorMetodoPago,
            List<ProductoVendidoDto> masVendidos,
            List<ProductoStockBajoDto> stockBajo,
            List<OrdenDto> ultimasOrdenes) {}
}
