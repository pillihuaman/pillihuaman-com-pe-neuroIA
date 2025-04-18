package pillihuaman.com.pe.neuroIA.RequestResponse.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ReqControl {
    private ObjectId id;
    private String idCode;
    private int idMenu;
    private int idSystem;
    private int idPage;
    private String description;
    private String icono;
    private String iconClass;
    private int status;
    private String styleClass;
    private ObjectId id_user;
    private String text;
}