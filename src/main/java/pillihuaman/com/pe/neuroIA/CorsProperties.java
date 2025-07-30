package pillihuaman.com.pe.neuroIA;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mapea las propiedades de configuración de CORS desde las variables de entorno
 * (prefijo 'cors') a un objeto Java.
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Data // Lombok genera automáticamente getters, setters, etc.
public class CorsProperties {
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private boolean allowCredentials;
}