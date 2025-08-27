package pillihuaman.com.pe.neuroIA.config;



import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pillihuaman.com.pe.lib.exception.ErrorResponseApiGeneric;

/**
 * Manejador de excepciones global para la API. Esta clase está anotada con
 * @RestControllerAdvice, lo que permite a Spring interceptar excepciones
 * lanzadas desde cualquier @RestController en la aplicación.
 */
@RestControllerAdvice
@Slf4j
public class GlobalApiExceptionHandler {

    /**
     * Manejador específico para la excepción PromptNotFoundException.
     * La anotación @ExceptionHandler le dice a Spring que ejecute este método
     * cada vez que un controlador lance una PromptNotFoundException.
     *
     * @param ex      La excepción PromptNotFoundException capturada.
     * @param request El contexto de la petición web actual (útil para logging avanzado).
     * @return Un ResponseEntity que contiene el cuerpo del error estandarizado de tu librería
     *         y el estado HTTP 404 (Not Found).
     */
    @ExceptionHandler(PromptNotFoundException.class)
    public ResponseEntity<ErrorResponseApiGeneric> handlePromptNotFound(
            PromptNotFoundException ex, WebRequest request) {

        // Loguear el error en el servidor para trazabilidad y depuración.
        log.error("Error de configuración: No se encontró la plantilla de prompt. Causa: {}", ex.getMessage());

        // AQUÍ ES DONDE SE USA LA LIBRERÍA PARA CONSTRUIR LA RESPUESTA FINAL
        final ErrorResponseApiGeneric errorResponse = new ErrorResponseApiGeneric(
                HttpStatus.NOT_FOUND.value(),      // 404
                "Error de configuración interna",  // Mensaje general para el cliente
                ex.getMessage()                    // Mensaje detallado del error
        );

        // Se empaqueta en una respuesta HTTP y se envía al cliente.
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Manejador genérico para cualquier otra excepción no controlada.
     * Es una buena práctica tener un "catch-all" para evitar que los errores 500
     * expongan trazas de la pila (stack traces) al cliente.
     *
     * @param ex La excepción genérica capturada.
     * @param request El contexto de la petición web.
     * @return Una respuesta de error estandarizada con estado HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseApiGeneric> handleGenericException(Exception ex, WebRequest request) {
        log.error("Ha ocurrido un error inesperado en el servidor.", ex);
        final ErrorResponseApiGeneric errorResponse = new ErrorResponseApiGeneric(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, intente de nuevo más tarde."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}