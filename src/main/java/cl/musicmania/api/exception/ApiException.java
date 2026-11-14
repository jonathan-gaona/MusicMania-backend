package cl.musicmania.api.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe devolver la API. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
