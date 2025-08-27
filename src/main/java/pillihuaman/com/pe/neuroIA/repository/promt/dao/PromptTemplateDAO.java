package pillihuaman.com.pe.neuroIA.repository.promt.dao;

import pillihuaman.com.pe.neuroIA.repository.BaseMongoRepository;
import pillihuaman.com.pe.neuroIA.repository.promt.PromptTemplate;

import java.util.Optional;

/**
 * Interfaz del Objeto de Acceso a Datos para las plantillas de prompt.
 * Define las operaciones específicas para interactuar con la colección 'prompt_templates'.
 */
public interface PromptTemplateDAO extends BaseMongoRepository<PromptTemplate> {

    /**
     * Busca una plantilla de prompt activa por su identificador único legible.
     *
     * @param promptId El ID legible de la plantilla (ej: "GLOBAL_SEARCH_INTENT_V1").
     * @return El documento de la plantilla de prompt si se encuentra uno activo.
     */
    PromptTemplate findActiveById(String promptId);

    Optional<PromptTemplate> findByPromptId(String promptId);
}