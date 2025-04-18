package pillihuaman.com.pe.neuroIA.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ReqIa {
    private String id;
    private String textIA;
    private String descriptionIA;
}


