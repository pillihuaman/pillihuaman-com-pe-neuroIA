package pillihuaman.com.pe.neuroIA.repository.promt;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pillihuaman.com.pe.lib.common.AuditEntity;

import java.util.Map;

/**
 * Representa una plantilla de prompt para interactuar con modelos de IA.
 * Esta estructura permite gestionar los prompts como configuración dinámica
 * en la base de datos en lugar de hardcodearlos en el código.
 */
@Data
@Document(collection = "prompt_templates")
public class PromptTemplate {

    @Id
    private ObjectId id;

    /**
     * Un identificador único legible por humanos para la plantilla, ej: "GLOBAL_SEARCH_INTENT_V1".
     * Usado para buscar la plantilla desde el código.
     */
    private String promptId;

    /**
     * Versión numérica de la plantilla para control de cambios.
     */
    private int version;

    /**
     * Descripción de para qué se utiliza esta plantilla de prompt.
     */
    private String description;

    /**
     * Estado de la plantilla, permite activar/desactivar versiones.
     * Valores posibles: "ACTIVE", "INACTIVE", "ARCHIVED".
     */
    private String status;

    /**
     * La plantilla base del prompt de sistema, con placeholders como {{json_structure}} y {{examples}}.
     */
    private String systemPromptTemplate;

    /**
     * Un mapa que contiene los datos dinámicos que se inyectarán en la plantilla.
     * Esto permite que la estructura JSON y los ejemplos se gestionen como datos, no como texto plano.
     */
    private Map<String, Object> parameters;

    /**
     * Información de auditoría sobre quién creó/modificó la plantilla.
     */
    private AuditEntity audit;
}