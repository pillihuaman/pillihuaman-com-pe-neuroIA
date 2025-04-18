package pillihuaman.com.pe.neuroIA.repository.files;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "file_metadata")
public class FileMetadata {
    @Id
    private String id;
    private String filename;
    private String s3Key;
    private String contentType;
    private long size;
    private String hashCode;
    private String dimension; // e.g., "front", "posterior", etc.
    private String userId;
    private long uploadTimestamp;
    private boolean status = true; // true = active, false = inactive

}
