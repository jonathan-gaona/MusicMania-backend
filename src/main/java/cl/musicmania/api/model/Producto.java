package cl.musicmania.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTO")
public class Producto {

    @Id
    @Column(name = "id_producto", length = 30)
    private String id;

    /** vinilos | equipos | accesorios | ofertas */
    @Column(name = "categoria", nullable = false, length = 30)
    private String categoria;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "artista", length = 100)
    private String artista;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "sku", length = 60)
    private String sku;

    @Column(name = "precio", nullable = false)
    private Integer precio;

    @Column(name = "precio_antes")
    private Integer precioAntes;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "imagen")
    private String imagen;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "spotify_url", length = 400)
    private String spotifyUrl;

    // Lo llena MySQL con CURRENT_TIMESTAMP
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }
    public Integer getPrecioAntes() { return precioAntes; }
    public void setPrecioAntes(Integer precioAntes) { this.precioAntes = precioAntes; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public String getSpotifyUrl() { return spotifyUrl; }
    public void setSpotifyUrl(String spotifyUrl) { this.spotifyUrl = spotifyUrl; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
