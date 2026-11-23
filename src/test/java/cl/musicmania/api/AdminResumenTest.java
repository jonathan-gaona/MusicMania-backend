package cl.musicmania.api;

import cl.musicmania.api.model.Producto;
import cl.musicmania.api.model.Usuario;
import cl.musicmania.api.repository.OrdenRepository;
import cl.musicmania.api.repository.ProductoRepository;
import cl.musicmania.api.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas del endpoint GET /api/v1/admin/resumen (cifras reales del panel de administración). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminResumenTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired ProductoRepository productos;
    @Autowired OrdenRepository ordenes;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void datosBase() {
        ordenes.deleteAll();
        productos.deleteAll();
        usuarios.deleteAll();

        crearUsuario("Admin", "admin@duoc.cl", "admin", "Administrador");
        crearUsuario("Cliente", "cliente@gmail.com", "1234", "Cliente");

        Producto p = new Producto();
        p.setId("alb-1");
        p.setCategoria("vinilos");
        p.setTitulo("Disco de prueba");
        p.setPrecio(10000);
        p.setStock(5);
        productos.save(p);
    }

    private void crearUsuario(String nombre, String correo, String password, String rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(password));
        u.setRol(rol);
        usuarios.save(u);
    }

    private String token(String correo, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    @Test
    void resumenSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/admin/resumen")).andExpect(status().isUnauthorized());
    }

    @Test
    void resumenComoClienteDevuelve403() throws Exception {
        mvc.perform(get("/api/v1/admin/resumen").header("Authorization", "Bearer " + token("cliente@gmail.com", "1234")))
                .andExpect(status().isForbidden());
    }

    @Test
    void resumenComoAdminTraeLasCifrasDeLaBaseDeDatos() throws Exception {
        // Compra de invitado: 3 unidades a $10.000 -> total $30.000 y stock restante 2 (stock bajo)
        mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cliente\":{\"nombre\":\"Ana\",\"apellidos\":\"Pérez\",\"correo\":\"ana@gmail.com\","
                                + "\"calle\":\"Calle 1\",\"comuna\":\"La Serena\"},"
                                + "\"items\":[{\"id\":\"alb-1\",\"cantidad\":3}]}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/admin/resumen").header("Authorization", "Bearer " + token("admin@duoc.cl", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(2))
                .andExpect(jsonPath("$.clientes").value(1))
                .andExpect(jsonPath("$.administradores").value(1))
                .andExpect(jsonPath("$.totalProductos").value(1))
                .andExpect(jsonPath("$.unidadesEnStock").value(2))
                .andExpect(jsonPath("$.totalOrdenes").value(1))
                .andExpect(jsonPath("$.ventasTotales").value(30000))
                .andExpect(jsonPath("$.ordenesPorEstado.Pendiente").value(1))
                .andExpect(jsonPath("$.masVendidos[0].id").value("alb-1"))
                .andExpect(jsonPath("$.masVendidos[0].unidades").value(3))
                .andExpect(jsonPath("$.stockBajo[0].id").value("alb-1"))
                .andExpect(jsonPath("$.ultimasOrdenes[0].total").value(30000));
    }
}
