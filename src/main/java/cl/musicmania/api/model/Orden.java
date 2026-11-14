package cl.musicmania.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDEN")
public class Orden {

    @Id
    @Column(name = "id_orden", length = 30)
    private String id;

    /** null si compró como invitado */
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "cliente_nombre", nullable = false, length = 160)
    private String clienteNombre;

    @Column(name = "correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "rut", length = 12)
    private String rut;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "region", length = 80)
    private String region;

    @Column(name = "comuna", length = 80)
    private String comuna;

    @Column(name = "indicaciones", length = 2000)
    private String indicaciones;

    @Column(name = "metodo_pago", nullable = false, length = 60)
    private String metodoPago;

    /** Pendiente | En preparación | Enviado | Entregado | Cancelado */
    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrden> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleOrden d) {
        d.setOrden(this);
        detalles.add(d);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public List<DetalleOrden> getDetalles() { return detalles; }
}
