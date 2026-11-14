package cl.musicmania.api.config;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger: http://localhost:4000/swagger-ui.html */
@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI musicManiaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("MusicMania API")
                .version("1.0")
                .description("API REST de la tienda MusicMania: catálogo, usuarios, órdenes y autenticación con JWT. "
                        + "Para probar endpoints protegidos: POST /api/auth/login, copia el token y presiona 'Authorize'."));
    }
}
