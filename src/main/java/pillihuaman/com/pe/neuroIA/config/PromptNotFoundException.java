package pillihuaman.com.pe.neuroIA.config;


/**
 * Excepción específica que se lanza cuando una plantilla de prompt requerida
 * no se encuentra en la base de datos o no está activa.
 * Extiende RuntimeException para que no sea necesario declararla en cada firma de método (unchecked).
 */
public class PromptNotFoundException extends RuntimeException {

    public PromptNotFoundException(String message) {
        super(message);
    }
}