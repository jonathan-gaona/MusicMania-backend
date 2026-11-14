package cl.musicmania.api.service;

import cl.musicmania.api.dto.Dtos.*;
import cl.musicmania.api.dto.Mapeos;
import cl.musicmania.api.exception.ApiException;
import cl.musicmania.api.model.DetalleOrden;
import cl.musicmania.api.model.Orden;
import cl.musicmania.api.model.Producto;
import cl.musicmania.api.repository.OrdenRepository;
import cl.musicmania.api.repository.ProductoRepository;
import cl.musicmania.api.repository.UsuarioRepository;
import cl.musicmania.api.security.SesionUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrdenService {

    public static final List<String> ESTADOS = List.of("Pendiente", "En preparación", "Enviado", "Entregado", "Cancelado");

    private final OrdenRepository ordenes;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public OrdenService(OrdenRepository ordenes, ProductoRepository productos, UsuarioRepository usuarios) {
        this.ordenes = ordenes;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /**
     * Checkout. Regla de negocio principal del sistema:
     *  1. El precio se toma SIEMPRE de la base de datos (nunca del navegador).
     *  2. Se valida y descuenta el stock dentro de una transacción (si algo falla, no se guarda nada).
     *  3. Acepta compras de invitados (sin sesión) y de clientes autenticados.
     */
    @Transactional
    public OrdenDto crear(OrdenRequest r, SesionUsuario sesion) {
        ClienteDto c = r.cliente();
        String nombre = c == null ? "" : ((c.nombre() == null ? "" : c.nombre()) + " "
                + (c.apellidos() == null ? "" : c.apellidos())).trim();
        if (c == null || nombre.isEmpty() || vacio(c.correo()) || vacio(c.calle()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Faltan datos del cliente o de la dirección.");
        if (r.items() == null || r.items().isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito está vacío.");

        // Une ítems repetidos del mismo producto
        Map<String, Integer> pedido = new LinkedHashMap<>();
        for (ItemDto it : r.items()) {
            int cantidad = it.cantidad() == null ? 1 : it.cantidad();
            pedido.merge(it.id(), cantidad, Integer::sum);
        }

        Orden orden = new Orden();
        int total = 0;
        for (Map.Entry<String, Integer> e : pedido.entrySet()) {
            Producto p = productos.findForUpdate(e.getKey())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Producto no encontrado: " + e.getKey()));
            int cantidad = e.getValue();
            if (cantidad < 1 || p.getStock() < cantidad)
                throw new ApiException(HttpStatus.CONFLICT,
                        "Stock insuficiente para \"" + p.getTitulo() + "\" (disponible: " + p.getStock() + ").");

            total += p.getPrecio() * cantidad;
            p.setStock(p.getStock() - cantidad);

            DetalleOrden d = new DetalleOrden();
            d.setIdProducto(p.getId());
            d.setNombre(p.getTitulo());
            d.setPrecioUnitario(p.getPrecio());
            d.setCantidad(cantidad);
            orden.agregarDetalle(d);
        }

        if (sesion != null) {
            usuarios.findById(sesion.idUsuario()).ifPresent(u -> {
                orden.setIdUsuario(u.getId());
                orden.setRut(u.getRun());
            });
        }

        orden.setId(generarId());
        orden.setClienteNombre(nombre);
        orden.setCorreo(c.correo().trim().toLowerCase());
        orden.setDireccion(List.of(c.calle(), c.depto() == null ? "" : c.depto(), c.comuna() == null ? "" : c.comuna())
                .stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining(", ")));
        orden.setRegion(c.region());
        orden.setComuna(c.comuna());
        orden.setIndicaciones(c.indicaciones());
        orden.setMetodoPago(vacio(r.metodoPago()) ? "WebPay (Simulado)" : r.metodoPago());
        orden.setEstado("Pendiente");
        orden.setTotal(total);
        orden.setFecha(LocalDateTime.now(ZoneId.of("America/Santiago")));

        return Mapeos.orden(ordenes.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenDto> listarTodas() {
        return ordenes.findAllByOrderByFechaDesc().stream().map(Mapeos::orden).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenDto> misOrdenes(Long idUsuario) {
        return ordenes.findByIdUsuarioOrderByFechaDesc(idUsuario).stream().map(Mapeos::orden).toList();
    }

    @Transactional
    public MensajeDto cambiarEstado(String id, String estado) {
        if (!ESTADOS.contains(estado)) throw new ApiException(HttpStatus.BAD_REQUEST, "Estado inválido.");
        buscar(id).setEstado(estado);
        return new MensajeDto("Estado actualizado.");
    }

    @Transactional
    public MensajeDto eliminar(String id) {
        ordenes.delete(buscar(id));
        return new MensajeDto("Orden eliminada.");
    }

    private Orden buscar(String id) {
        return ordenes.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada."));
    }

    private String generarId() {
        String id;
        do {
            id = "BOL-" + LocalDateTime.now().getYear() + "-" + ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (ordenes.existsById(id));
        return id;
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
