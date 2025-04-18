package pillihuaman.com.pe.neuroIA.repository.files.dao.implement;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pillihuaman.com.pe.neuroIA.Help.Constantes;
import pillihuaman.com.pe.neuroIA.dto.ReqFile;
import pillihuaman.com.pe.neuroIA.repository.AzureAbstractMongoRepositoryImpl;
import pillihuaman.com.pe.neuroIA.repository.files.FileMetadata;
import pillihuaman.com.pe.neuroIA.repository.files.dao.FilesDAO;

import java.util.List;

@Component
@Repository
public class FilesDaoImplement extends AzureAbstractMongoRepositoryImpl<FileMetadata> implements FilesDAO {
    FilesDaoImplement() {
        DS_WRITE = Constantes.DW;
        COLLECTION = Constantes.COLLECTION_FILE_METADATA;
    }

    @Override
    public Class<FileMetadata> provideEntityClass() {
        return FileMetadata.class;
    }


    @Override
    public List<FileMetadata> getFilesResponse(ReqFile reqFile) {
        return List.of();
    }
}
