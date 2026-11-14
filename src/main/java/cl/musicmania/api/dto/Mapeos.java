package cl.musicmania.api.dto;

import cl.musicmania.api.dto.Dtos.*;
import cl.musicmania.api.model.Orden;
import cl.musicmania.api.model.Producto;
import cl.musicmania.api.model.Usuario;

import java.time.format.DateTimeFormatter;

/** Convierte entidades de la base de datos al formato JSON que consume el frontend. */
public final class Mapeos {
    private Mapeos() {}

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static UsuarioDto usuario(Usuario u) {
        return new UsuarioDto(u.getId(), u.getRun() == null ? "" : u.getRun(), u.getNombre(), u.getCorreo(),
                u.getRegion(), u.getComuna(), u.getDireccion(), u.getRol());
    }

    public static ProductoDto producto(Producto p) {
        return new ProductoDto(p.getId(), p.getCategoria(), p.getTitulo(), p.getTitulo(), p.getArtista(),
                p.getDescripcion(), p.getSku(), p.getPrecio(), p.getPrecioAntes(), p.getStock(),
                p.getImagen(), p.getAnio(), p.getSpotifyUrl());
    }

    public static OrdenDto orden(Orden o) {
        var lineas = o.getDetalles().stream()
                .map(d -> new LineaDto(d.getIdProducto(), d.getNombre(), d.getPrecioUnitario(), d.getCantidad()))
                .toList();
        return new OrdenDto(o.getId(), "#" + o.getId(),
                o.getFecha() == null ? null : o.getFecha().format(FECHA),
                o.getClienteNombre(), o.getCorreo(), o.getRut() == null ? "" : o.getRut(),
                o.getDireccion(), o.getMetodoPago(), o.getEstado(), o.getTotal(), lineas);
    }
}
