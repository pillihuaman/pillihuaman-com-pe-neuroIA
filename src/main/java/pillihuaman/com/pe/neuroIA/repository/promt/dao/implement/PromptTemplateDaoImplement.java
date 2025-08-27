package pillihuaman.com.pe.neuroIA.repository.promt.dao.implement;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.repository.AzureAbstractMongoRepositoryImpl;
import pillihuaman.com.pe.neuroIA.repository.promt.PromptTemplate;
import pillihuaman.com.pe.neuroIA.repository.promt.dao.PromptTemplateDAO;

import java.util.Optional;


@Component
@Repository
public class PromptTemplateDaoImplement extends AzureAbstractMongoRepositoryImpl<PromptTemplate> implements PromptTemplateDAO {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateDaoImplement.class);

    // Constructor que define la colección a utilizar
    public PromptTemplateDaoImplement() {
        DS_WRITE = Constante.DW;
        COLLECTION = "prompt_templates"; // Nombre de la nueva colección
    }

    @Override
    public Class<PromptTemplate> provideEntityClass() {
        return PromptTemplate.class;
    }

    /**
     * Implementación del método para buscar una plantilla activa.
     * El resultado de esta consulta se cachea para un rendimiento óptimo.
     */
    @Override
    //@Cacheable(value = CacheConfig.PROMPT_TEMPLATE_CACHE, key = "#promptId")
    public PromptTemplate findActiveById(String promptId) {
        log.info("===================== BUSCANDO PROMPT EN BD (CACHE MISS) PARA ID: {} =====================", promptId);

        // Construimos un filtro compuesto: debe coincidir el 'promptId' Y el 'status' debe ser "ACTIVE"
        Bson query = Filters.and(
                Filters.eq("promptId", promptId),
                Filters.eq("status", "ACTIVE")
        );

        log.info("--- Consulta BSON generada para MongoDB: {}", query);

        MongoCollection<PromptTemplate> collection = getCollection(this.COLLECTION, provideEntityClass());
        PromptTemplate result = collection.find(query).first();

        if (result != null) {
            log.info("--- Se encontró un prompt activo. Descripción: {}", result.getDescription());
        } else {
            log.warn("--- ¡ADVERTENCIA! No se encontró ningún prompt activo para el ID: {}", promptId);
        }
        log.info("===================== BÚSQUEDA DE PROMPT FINALIZADA =================================");

        return result;
    }

    /**
     * Imprime el contenido de la colección al iniciar la aplicación (útil para depuración).
     */
    @PostConstruct
    public void init() {
        super.printAllDataFromCollection(this.COLLECTION);
    }

    @Override
    public Optional<PromptTemplate> findByPromptId(String promptId) {
        log.info("Verificando existencia del prompt con ID: {}", promptId);
        Bson query = Filters.eq("promptId", promptId);
        MongoCollection<PromptTemplate> collection = getCollection(this.COLLECTION, provideEntityClass());
        // Usamos Optional.ofNullable para manejar el caso en que no se encuentre nada.
        return Optional.ofNullable(collection.find(query).first());
    }
}