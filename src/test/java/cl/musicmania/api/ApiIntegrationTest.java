package cl.musicmania.api;

import cl.musicmania.api.model.Producto;
import cl.musicmania.api.model.Usuario;
import cl.musicmania.api.repository.OrdenRepository;
import cl.musicmania.api.repository.ProductoRepository;
import cl.musicmania.api.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración: levantan toda la aplicación (controladores +
 * seguridad JWT + JPA)
 * contra una base H2 en memoria y llaman a la API como lo haría el frontend.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UsuarioRepository usuarios;
    @Autowired
    ProductoRepository productos;
    @Autowired
    OrdenRepository ordenes;
    @Autowired
    PasswordEncoder encoder;

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

    private JsonNode login(String correo, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("alb-1"));
    }

    @Test
    void loginConCredencialesIncorrectasDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"admin@duoc.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearProductoSinTokenDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Nuevo\",\"precio\":5000}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearProductoComoClienteDevuelve403() throws Exception {
        String token = login("cliente@gmail.com", "1234").get("token").asText();
        mvc.perform(post("/api/v1/productos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Nuevo\",\"precio\":5000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearProductoComoAdminDevuelve201() throws Exception {
        String token = login("admin@duoc.cl", "admin").get("token").asText();
        mvc.perform(post("/api/v1/productos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Nuevo\",\"precio\":5000,\"categoria\":\"equipos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Nuevo"));
    }

    @Test
    void checkoutDescuentaStockYUsaPrecioDelServidor() throws Exception {
        mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cliente\":{\"nombre\":\"Ana\",\"apellidos\":\"Pérez\",\"correo\":\"ana@gmail.com\","
                        + "\"calle\":\"Calle 1\",\"comuna\":\"La Serena\"},"
                        + "\"items\":[{\"id\":\"alb-1\",\"cantidad\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(20000))
                .andExpect(jsonPath("$.estado").value("Pendiente"));

        mvc.perform(get("/api/v1/productos/alb-1"))
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void checkoutConStockInsuficienteDevuelve409() throws Exception {
        mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cliente\":{\"nombre\":\"Ana\",\"correo\":\"ana@gmail.com\",\"calle\":\"Calle 1\"},"
                        + "\"items\":[{\"id\":\"alb-1\",\"cantidad\":99}]}"))
                .andExpect(status().isConflict());
    }

    @Test
    void refreshTokenEmiteUnAccessTokenNuevo() throws Exception {
        String refresh = login("cliente@gmail.com", "1234").get("refreshToken").asText();
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void refreshTokenNoSirveComoAccessToken() throws Exception {
        String refresh = login("admin@duoc.cl", "admin").get("refreshToken").asText();
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
    }
}
