# MusicMania API (Spring Boot)

Backend REST de MusicMania: Spring Boot 3, Spring Data JPA (MySQL), Spring Security + JWT con roles y documentación Swagger.

## Requisitos
- Java 17 o superior (`java -version`)
- MySQL con la base `musicmania` ya creada (scripts en `database/`: `01_schema.sql` y `02_seed.sql`) o la base de Aiven
- IntelliJ IDEA (incluye Maven) o VS Code con "Extension Pack for Java" y "Spring Boot Extension Pack"

## Cómo ejecutarlo

### 1. Credenciales de la base de datos
Copia `src/main/resources/application-local.properties.example` como `src/main/resources/application-local.properties` y completa tus datos (este archivo no se sube a GitHub).

### 2. IntelliJ IDEA
1. **File → Open** y elige esta carpeta (donde está `pom.xml`). Espera a que Maven descargue las dependencias.
2. Abre `MusicManiaApplication.java` y ejecútala con el botón verde.
3. **Run → Edit Configurations → VM options:** `-Dspring.profiles.active=local` y vuelve a ejecutar.

### 3. VS Code
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

La API queda en `http://localhost:4000`. Si el backend en Node está corriendo, detenlo (usa el mismo puerto).

## Swagger (documentación interactiva)
`http://localhost:4000/swagger-ui.html`

Para probar endpoints protegidos: ejecuta `POST /api/auth/login`, copia el valor de `token` y presiona **Authorize** (arriba a la derecha).

## Endpoints

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/register` | Público |
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/refresh` | Público (requiere refresh token) |
| GET | `/api/auth/me` | Sesión |
| GET | `/api/productos`, `/api/productos/{id}` | Público |
| POST / PUT / DELETE | `/api/productos`, `/api/productos/{id}` | ADMINISTRADOR |
| POST | `/api/ordenes` (checkout, acepta invitados) | Público |
| GET | `/api/ordenes/mias` | Sesión |
| GET / PATCH / DELETE | `/api/ordenes`, `/api/ordenes/{id}/estado`, `/api/ordenes/{id}` | ADMINISTRADOR |
| GET / POST / PUT / DELETE | `/api/usuarios`, `/api/usuarios/{id}` | ADMINISTRADOR |

## Seguridad (JWT)
- El login devuelve un **access token** (30 min) y un **refresh token** (7 días). Ambos van firmados (HS384) con `JWT_SECRET`.
- Cada petición protegida envía `Authorization: Bearer <access token>`. `JwtAuthFilter` valida firma y expiración y carga el rol del usuario.
- `SecurityConfig` define qué rutas son públicas, cuáles requieren sesión y cuáles solo el rol ADMINISTRADOR (401 sin token, 403 con rol insuficiente).
- Cuando el access token vence, el frontend llama a `/api/auth/refresh` con el refresh token y recibe tokens nuevos. Un refresh token no sirve para acceder a recursos.
- Las contraseñas se guardan con BCrypt.

## Variables de entorno (producción)
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSL_MODE=REQUIRED` (Aiven), `JWT_SECRET` (mínimo 32 caracteres), `FRONTEND_URL` (origen permitido por CORS), `SEED_USUARIOS`.

## Pruebas de integración
```bash
mvn test
```
`ApiIntegrationTest` levanta la aplicación completa con una base H2 en memoria y verifica: catálogo público, 401 sin token, 403 con rol Cliente, 201 con rol Administrador, checkout con descuento de stock y precio del servidor, 409 por stock insuficiente y renovación de token.

## Estructura
```
src/main/java/cl/musicmania/api
├── config/       SecurityConfig, OpenApiConfig, DataInitializer
├── controller/   Endpoints REST (Auth, Producto, Usuario, Orden)
├── service/      Lógica de negocio
├── repository/   Acceso a datos (Spring Data JPA)
├── model/        Entidades JPA (Usuario, Producto, Orden, DetalleOrden)
├── dto/          Objetos JSON de entrada y salida
├── security/     JwtService, JwtAuthFilter
└── exception/    Manejo global de errores
```
