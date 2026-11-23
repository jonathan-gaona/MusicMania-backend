package cl.musicmania.api.service;

import cl.musicmania.api.dto.Dtos.*;
import cl.musicmania.api.dto.Mapeos;
import cl.musicmania.api.model.DetalleOrden;
import cl.musicmania.api.model.Orden;
import cl.musicmania.api.model.Producto;
import cl.musicmania.api.model.Usuario;
import cl.musicmania.api.repository.OrdenRepository;
import cl.musicmania.api.repository.ProductoRepository;
import cl.musicmania.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resumen del panel de administración: todas las cifras salen de la base de datos.
 * Se calcula en memoria porque el volumen de una tienda de este tamaño es pequeño
 * y así funciona igual en MySQL (producción) y H2 (pruebas).
 */
@Service
public class AdminService {

    private static final String CANCELADO = "Cancelado";
    private static final int STOCK_BAJO = 3;

    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final OrdenRepository ordenes;

    public AdminService(UsuarioRepository usuarios, ProductoRepository productos, OrdenRepository ordenes) {
        this.usuarios = usuarios;
        this.productos = productos;
        this.ordenes = ordenes;
    }

    @Transactional(readOnly = true)
    public ResumenAdminDto resumen() {
        List<Usuario> listaUsuarios = usuarios.findAll();
        List<Producto> listaProductos = productos.findAll();
        List<Orden> listaOrdenes = ordenes.findAllByOrderByFechaDesc();

        // Las órdenes canceladas no cuentan como venta
        List<Orden> vendidas = listaOrdenes.stream().filter(o -> !CANCELADO.equals(o.getEstado())).toList();
        long ventasTotales = vendidas.stream().mapToLong(Orden::getTotal).sum();
        long ticketPromedio = vendidas.isEmpty() ? 0 : Math.round((double) ventasTotales / vendidas.size());

        // Conteo por estado (todos los estados aparecen, aunque tengan 0)
        Map<String, Integer> porEstado = new LinkedHashMap<>();
        OrdenService.ESTADOS.forEach(e -> porEstado.put(e, 0));
        listaOrdenes.forEach(o -> porEstado.merge(o.getEstado(), 1, Integer::sum));

        // Ventas por método de pago
        Map<String, Long> porMetodo = new LinkedHashMap<>();
        vendidas.forEach(o -> porMetodo.merge(o.getMetodoPago(), (long) o.getTotal(), Long::sum));

        // Productos más vendidos (agrupados por id; el nombre es el de la compra)
        Map<String, ProductoVendidoAcum> acumulado = new LinkedHashMap<>();
        for (Orden o : vendidas) {
            for (DetalleOrden d : o.getDetalles()) {
                String clave = d.getIdProducto() != null ? d.getIdProducto() : d.getNombre();
                ProductoVendidoAcum a = acumulado.computeIfAbsent(clave,
                        k -> new ProductoVendidoAcum(d.getIdProducto(), d.getNombre()));
                a.unidades += d.getCantidad();
                a.ingresos += (long) d.getPrecioUnitario() * d.getCantidad();
            }
        }
        List<ProductoVendidoDto> masVendidos = acumulado.values().stream()
                .sorted(Comparator.comparingLong((ProductoVendidoAcum a) -> a.unidades).reversed())
                .limit(5)
                .map(a -> new ProductoVendidoDto(a.id, a.nombre, a.unidades, a.ingresos))
                .toList();

        // Productos con poco stock
        List<ProductoStockBajoDto> stockBajo = listaProductos.stream()
                .filter(p -> p.getStock() <= STOCK_BAJO)
                .sorted(Comparator.comparingInt(Producto::getStock))
                .map(p -> new ProductoStockBajoDto(p.getId(), p.getTitulo(), p.getCategoria(), p.getStock()))
                .toList();

        return new ResumenAdminDto(
                listaUsuarios.size(),
                contarRol(listaUsuarios, "Cliente"),
                contarRol(listaUsuarios, "Administrador"),
                contarRol(listaUsuarios, "Vendedor"),
                listaProductos.size(),
                listaProductos.stream().mapToLong(Producto::getStock).sum(),
                stockBajo.size(),
                listaOrdenes.size(),
                ventasTotales,
                ticketPromedio,
                porEstado,
                porMetodo,
                masVendidos,
                stockBajo,
                listaOrdenes.stream().limit(5).map(Mapeos::orden).toList());
    }

    private static int contarRol(List<Usuario> lista, String rol) {
        return (int) lista.stream().filter(u -> rol.equals(u.getRol())).count();
    }

    /** Acumulador interno para armar el ranking de productos vendidos. */
    private static final class ProductoVendidoAcum {
        final String id;
        final String nombre;
        long unidades;
        long ingresos;

        ProductoVendidoAcum(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }
}
