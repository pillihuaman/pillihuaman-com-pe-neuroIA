package pillihuaman.com.pe.neuroIA.Service.Implement;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pillihuaman.com.pe.neuroIA.config.PromptNotFoundException;
import pillihuaman.com.pe.neuroIA.repository.promt.PromptTemplate;
import pillihuaman.com.pe.neuroIA.repository.promt.dao.PromptTemplateDAO;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptManagerService {
    @Autowired
    private  PromptTemplateDAO promptTemplateDAO;

    @Autowired
    private  ObjectMapper objectMapper;

    /**
     * Obtiene y ensambla un prompt dinámicamente desde la base de datos.
     *
     * @param promptId El ID del prompt a buscar (ej: "GLOBAL_SEARCH_INTENT_V1").
     * @return El string del prompt final listo para ser enviado a la IA.
     * @throws PromptNotFoundException si no se encuentra un prompt activo con el ID proporcionado.
     */
    public String getAssembledPrompt(String promptId) {
        // La llamada al DAO ya está cacheada, así que no necesitamos @Cacheable aquí.
        PromptTemplate template = promptTemplateDAO.findActiveById(promptId);

        if (template == null) {
            log.error("Error crítico: No se encontró la plantilla de prompt activa '{}'. La funcionalidad de IA no puede continuar.", promptId);
            throw new PromptNotFoundException("No se encontró un prompt activo con ID: " + promptId);
        }

        try {
            // Ensamblar el prompt reemplazando los placeholders
            return template.getSystemPromptTemplate()
                    .replace("{{json_structure}}", formatParameterAsJson(template, "json_structure"))
                    .replace("{{examples}}", formatExamples(template));
        } catch (Exception e) {
            log.error("Error al ensamblar el prompt '{}'. Verifica la estructura de los parámetros en la BD.", promptId, e);
            throw new IllegalStateException("Fallo al construir el prompt desde la plantilla.", e);
        }
    }

    private String formatParameterAsJson(PromptTemplate template, String key) throws JsonProcessingException {
        Object param = template.getParameters().get(key);
        if (param == null) {
            log.warn("El parámetro '{}' no existe en la plantilla '{}'. Se usará un string vacío.", key, template.getPromptId());
            return "{}";
        }
        return objectMapper.writeValueAsString(param);
    }

    private String formatExamples(PromptTemplate template) {
        Object examplesParam = template.getParameters().get("examples");
        if (!(examplesParam instanceof List)) {
            log.warn("El parámetro 'examples' no es una lista en la plantilla '{}'. No se añadirán ejemplos.", template.getPromptId());
            return "";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> examples = (List<Map<String, String>>) examplesParam;
        StringBuilder examplesBuilder = new StringBuilder();
        examples.forEach(ex -> examplesBuilder.append("- Consulta: \"")
                .append(ex.get("query"))
                .append("\" -> ")
                .append(ex.get("expected"))
                .append("\n"));

        return examplesBuilder.toString().trim();
    }
}