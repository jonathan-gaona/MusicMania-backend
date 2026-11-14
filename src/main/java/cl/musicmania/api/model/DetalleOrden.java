package cl.musicmania.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DETALLE_ORDEN")
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden")
    private Orden orden;

    /** Se guarda solo el id: si el producto se elimina, el historial de la orden se conserva. */
    @Column(name = "id_producto", length = 30)
    private String idProducto;

    /** Copia del nombre al momento de la compra */
    @Column(name = "nombre", nullable = false, length = 160)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    public Long getId() { return id; }
    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }
    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Integer precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
