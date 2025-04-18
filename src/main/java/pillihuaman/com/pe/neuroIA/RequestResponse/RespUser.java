package pillihuaman.com.pe.neuroIA.RequestResponse;


import lombok.*;
import org.bson.types.ObjectId;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor // Add this annotation for the all-argument constructor
@Builder
@ToString
public class RespUser {
	
	private String idUser;
	private String alias;
	private  ObjectId idSystem;
	private String mail;
	private String mobilPhone;
	private String user;
	private String userName;
	private   boolean enabled;
	private ObjectId idRol;
	private String access_token;

}


