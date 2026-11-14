package cl.musicmania.api.config;

import cl.musicmania.api.model.Usuario;
import cl.musicmania.api.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea los usuarios de prueba (con contraseña hasheada) si no existen. Solo si app.seed.usuarios=true. */
@Component
@ConditionalOnProperty(name = "app.seed.usuarios", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DataInitializer(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        crear("Administrador Principal", "111111111", "admin@duoc.cl", "admin", "Administrador");
        crear("Administrador secundario", "222222222", "admin2@duoc.cl", "admin2", "Administrador");
        crear("Cliente Ejemplo", "444444444", "cliente@gmail.com", "1234", "Cliente");
        crear("Cliente Ejemplo 2", "33333333", "cliente2@gmail.com", "1234", "Cliente");
    }

    private void crear(String nombre, String run, String correo, String password, String rol) {
        if (usuarios.existsByCorreoIgnoreCase(correo)) return;
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setRun(run);
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(password));
        u.setRol(rol);
        usuarios.save(u);
    }
}
