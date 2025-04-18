package pillihuaman.com.pe.neuroIA.RequestResponse;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;


import java.util.List;

@Setter
@Getter
public class ResponseParameter {
    private ObjectId id;
    private String idCode;
    private String name;
    private String description;
    private List<String> parameterItems;
}
