package pillihuaman.com.pe.neuroIA.repository.files.dao;

import pillihuaman.com.pe.neuroIA.dto.ReqFile;
import pillihuaman.com.pe.neuroIA.repository.BaseMongoRepository;
import pillihuaman.com.pe.neuroIA.repository.files.FileMetadata;

import java.util.List;

public interface FilesDAO  extends BaseMongoRepository<FileMetadata> {

    /**WW
     * Lists stores based on specific request criteria.
     *
     * @param ReqFile The request criteria for filtering stores.
     * @return A list of stores matching the criteria.
     */
    List<FileMetadata> getFilesResponse(ReqFile reqFile);


}
