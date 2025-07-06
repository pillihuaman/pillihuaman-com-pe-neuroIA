package pillihuaman.com.pe.neuroIA.repository.files.dao.implement;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.dto.ReqFile;
import pillihuaman.com.pe.neuroIA.repository.AzureAbstractMongoRepositoryImpl;
import pillihuaman.com.pe.neuroIA.repository.files.FileMetadata;
import pillihuaman.com.pe.neuroIA.repository.files.dao.FilesDAO;

import java.util.ArrayList;
import java.util.List;

@Component
@Repository
public class FilesDaoImplement extends AzureAbstractMongoRepositoryImpl<FileMetadata> implements FilesDAO {
    FilesDaoImplement() {
        DS_WRITE = Constante.DW;
        COLLECTION = Constante.COLLECTION_FILE_METADATA;
    }

    @Override
    public Class<FileMetadata> provideEntityClass() {
        return FileMetadata.class;
    }


    @Override
    public List<FileMetadata> getFilesResponse(ReqFile reqFile) {
        return List.of();
    }



    @Override
    public List<FileMetadata> findAllByFilter(Bson query, Bson orderBy, Integer limit) {
        MongoCollection<FileMetadata> collection = getCollection(this.COLLECTION, FileMetadata.class);

        // Inicia la búsqueda
        FindIterable<FileMetadata> findIterable = collection.find(query);

        // Aplica la ordenación si se proporciona
        if (orderBy != null) {
            findIterable = findIterable.sort(orderBy);
        }

        // Aplica el límite si se proporciona
        if (limit != null && limit > 0) {
            findIterable = findIterable.limit(limit);
        }

        // Ejecuta la consulta y devuelve los resultados en una lista
        return findIterable.into(new ArrayList<>());
    }

    @Override
    public void deleteFileByQuery(Bson query) {
        MongoCollection<FileMetadata> collection = getCollection(this.COLLECTION, FileMetadata.class);
        collection.deleteOne(query);
    }

}
