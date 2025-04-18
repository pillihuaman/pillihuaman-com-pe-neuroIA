package pillihuaman.com.pe.neuroIA.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ReqFile {
    private String id;
    private String filename;
    private String s3Key;
    private String contentType;
    private long size;
    private String hashCode;
    private String dimension; // e.g., "front", "posterior", etc.
    private String userId;
    private long uploadTimestamp;
}


