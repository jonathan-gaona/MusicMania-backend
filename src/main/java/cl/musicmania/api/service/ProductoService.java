package cl.musicmania.api.service;

import cl.musicmania.api.dto.Dtos.MensajeDto;
import cl.musicmania.api.dto.Dtos.ProductoDto;
import cl.musicmania.api.dto.Dtos.ProductoRequest;
import cl.musicmania.api.dto.Mapeos;
import cl.musicmania.api.exception.ApiException;
import cl.musicmania.api.model.Producto;
import cl.musicmania.api.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    /** Sin categoría devuelve todo excepto las ofertas (que usa solo la página /ofertas). */
    @Transactional(readOnly = true)
    public List<ProductoDto> listar(String categoria) {
        List<Producto> lista = (categoria == null || categoria.isBlank())
                ? productos.findByCategoriaNot("ofertas")
                : productos.findByCategoria(categoria);
        return lista.stream()
                .sorted(Comparator.comparing(Producto::getCreadoEn, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparingInt((Producto p) -> p.getId().length())
                        .thenComparing(Producto::getId))
                .map(Mapeos::producto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(String id) {
        return Mapeos.producto(buscar(id));
    }

    @Transactional
    public ProductoDto crear(ProductoRequest r) {
        if (r.titulo() == null || r.titulo().isBlank() || r.precio() == null || r.precio() < 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Título y precio son obligatorios.");

        Producto p = new Producto();
        p.setId("prod-" + System.currentTimeMillis());
        p.setTitulo(r.titulo().trim());
        p.setCategoria(r.categoria() == null || r.categoria().isBlank() ? "vinilos" : r.categoria());
        p.setArtista(r.artista());
        p.setDescripcion(r.descripcion());
        p.setSku(r.sku());
        p.setPrecio(r.precio());
        p.setPrecioAntes(r.precioAntes());
        p.setStock(r.stock() == null ? 5 : r.stock());
        p.setImagen(r.imagen());
        p.setAnio(r.anio());
        p.setSpotifyUrl(r.spotifyUrl());
        return Mapeos.producto(productos.save(p));
    }

    /** Actualización parcial: solo cambia los campos que vienen en el JSON. */
    @Transactional
    public ProductoDto actualizar(String id, ProductoRequest r) {
        Producto p = buscar(id);
        if (r.titulo() != null) p.setTitulo(r.titulo());
        if (r.artista() != null) p.setArtista(r.artista());
        if (r.categoria() != null) p.setCategoria(r.categoria());
        if (r.descripcion() != null) p.setDescripcion(r.descripcion());
        if (r.sku() != null) p.setSku(r.sku());
        if (r.precio() != null) {
            if (r.precio() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "El precio no puede ser negativo.");
            p.setPrecio(r.precio());
        }
        if (r.precioAntes() != null) p.setPrecioAntes(r.precioAntes());
        if (r.stock() != null) {
            if (r.stock() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo.");
            p.setStock(r.stock());
        }
        if (r.imagen() != null) p.setImagen(r.imagen());
        if (r.anio() != null) p.setAnio(r.anio());
        if (r.spotifyUrl() != null) p.setSpotifyUrl(r.spotifyUrl());
        return Mapeos.producto(productos.save(p));
    }

    @Transactional
    public MensajeDto eliminar(String id) {
        productos.delete(buscar(id));
        return new MensajeDto("Producto eliminado.");
    }

    private Producto buscar(String id) {
        return productos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
    }
}
