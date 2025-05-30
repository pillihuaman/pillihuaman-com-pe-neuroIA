package pillihuaman.com.pe.neuroIA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RespFileMetadata {
    private String id;
    private String filename;
    private String contentType;
    private Long size;
    private String hashCode;
    private String dimension;
    private String userId;
    private Long uploadTimestamp;
    private Boolean status;
    private Integer order;
    private String typeFile;
    private String url;
    private String position;
    private String productId;
}
