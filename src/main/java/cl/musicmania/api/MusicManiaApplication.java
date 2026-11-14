package cl.musicmania.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Se excluye el usuario en memoria que Spring Security crea por defecto: los usuarios viven en MySQL.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MusicManiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicManiaApplication.class, args);
    }
}
