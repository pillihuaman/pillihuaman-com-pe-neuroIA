package pillihuaman.com.pe.neuroIA.repository.files.dao.implement;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.config.CacheConfig;
import pillihuaman.com.pe.neuroIA.dto.ReqFile;
import pillihuaman.com.pe.neuroIA.repository.AzureAbstractMongoRepositoryImpl;
import pillihuaman.com.pe.neuroIA.repository.files.FileMetadata;
import pillihuaman.com.pe.neuroIA.repository.files.dao.FilesDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.in;

@Component
@Repository
public class FilesDaoImplement extends AzureAbstractMongoRepositoryImpl<FileMetadata> implements FilesDAO {
    FilesDaoImplement() {
        DS_WRITE = Constante.DW;
        COLLECTION = Constante.COLLECTION_FILE_METADATA;
    }
    private static final Logger log = LoggerFactory.getLogger(FilesDaoImplement.class);
    @Override
    public Class<FileMetadata> provideEntityClass() {
        return FileMetadata.class;
    }


    @Override
    public List<FileMetadata> getFilesResponse(ReqFile reqFile) {
        return List.of();
    }

    @Override
    public List<FileMetadata> findAllByFilter(Bson query) {
        MongoCollection<FileMetadata> collection = getCollection(this.COLLECTION, FileMetadata.class);
        FindIterable<FileMetadata> findIterable = collection.find(query);
        return findIterable.into(new ArrayList<>());
    }

    @Override
    public List<FileMetadata> findAllByProductId(String productId) {
        // --- LOGGING DE DIAGNÓSTICO ---
        log.info("===================== INICIANDO BÚSQUEDA POR PRODUCT ID =====================");
        if (productId == null) {
            log.error("¡ERROR CRÍTICO! El productId recibido es NULL. No se puede buscar.");
            return new ArrayList<>(); // Devolvemos lista vacía
        }

        // Usamos [ ] para detectar visualmente espacios en blanco al principio o al final.
        log.info("--- 1. Valor de 'productId' recibido en el método: [{}]", productId);
        log.info("--- 2. Longitud del String 'productId' recibido: {}", productId.length());

        // Creamos el filtro. La lógica es correcta.
        Bson query = Filters.eq("productId", productId);
        log.info("--- 3. Consulta BSON generada para MongoDB: {}", query);
        log.info("-------------------------------------------------------------------------");

        List<FileMetadata> results = findAllByFilter(query);

        log.info("--- 4. Resultado: Se encontraron {} documentos.", results.size());
        log.info("===================== BÚSQUEDA FINALIZADA =================================");

        return results;
    }
    @Override
// La clave ahora se genera a partir del contenido de la lista 'productIds'
    @Cacheable(value = CacheConfig.FILE_METADATA_CACHE, key = "#productIds")
    public List<FileMetadata> findAllByProductIds(List<String> productIds) {
        log.info("===================== EJECUTANDO LÓGICA DE BÚSQUEDA EN BD (CACHE MISS) =====================");
        if (productIds == null || productIds.isEmpty()) {
            log.warn("La lista de productIds está vacía o es nula. Devolviendo una lista vacía.");
            return Collections.emptyList();
        }

        log.info("Buscando metadatos para {} IDs de producto.", productIds.size());
        Bson query = in("productId", productIds);
        log.info("Consulta BSON generada para MongoDB: {}", query);
        List<FileMetadata> results = findAllByFilter(query);
        log.info("--- 4. Resultado: Se encontraron {} documentos.", results.size());
        log.info("===================== BÚSQUEDA EN BD FINALIZADA =================================");

        return results;
    }
    @Override
    public FileMetadata findOneById(Bson query) {
        MongoCollection<FileMetadata> collection = getCollection(this.COLLECTION, FileMetadata.class);
        return collection.find(query).first(); // Retorna el primer documento o null si no se encuentra
    }


    @PostConstruct
    public void init() {
        // Esta línea ahora funcionará correctamente
        super.printAllDataFromCollection(this.COLLECTION);
    }
    @Override
    public void deleteFileByQuery(Bson query) {
        MongoCollection<FileMetadata> collection = getCollection(this.COLLECTION, FileMetadata.class);
        collection.deleteOne(query);
    }

}
