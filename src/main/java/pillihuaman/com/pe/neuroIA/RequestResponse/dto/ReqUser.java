package pillihuaman.com.pe.neuroIA.RequestResponse.dto;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@Builder
public class ReqUser implements  Serializable  {

	private String alias;
	private String email;
	private String mobilPhone;
	private String user;
	private String userName;
	private String apiPassword;
	private String password;
	private String salPassword;

	private  System system;

	public ReqUser(String alias, String email, String mobilPhone, String user, String userName, String apiPassword, String password, String salPassword, System system) {
		this.alias = alias;
		this.email = email;
		this.mobilPhone = mobilPhone;
		this.user = user;
		this.userName = userName;
		this.apiPassword = apiPassword;
		this.password = password;
		this.salPassword = salPassword;

		this.system = system;
	}

	public ReqUser(String alias, String email, String mobilPhone, String user, String userName, String apiPassword, String password, String salPassword) {
		this.alias = alias;
		this.email = email;
		this.mobilPhone = mobilPhone;
		this.user = user;
		this.userName = userName;
		this.apiPassword = apiPassword;
		this.password = password;
		this.salPassword = salPassword;
	}
	public ReqUser(){}
}


