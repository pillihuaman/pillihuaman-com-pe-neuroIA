package pillihuaman.com.pe.neuroIA.repository.files;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "file_metadata")
public class FileMetadata {
    @Id
    private ObjectId id;
    private String filename;
    private String s3Key;
    private String contentType;
    private long size;
    private String hashCode;
    private String dimension; // e.g., "front", "posterior", etc.
    private String userId;
    private long uploadTimestamp;
    private boolean status = true; // true = active, false = inactive
    private String typeFile = "CATALOG"; // Default type
    private int order = 1;
    private String position="CAT_IMG_1"; //Catalogo imagenes position  CAT_IMG_1 ,CAT_IMG_2
    private  String url;
    private String productId;


}
