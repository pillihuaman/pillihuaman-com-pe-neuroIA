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
public class RespIa {
    private ObjectId id;
    private String textIA;
    private String descriptionIA;
}


