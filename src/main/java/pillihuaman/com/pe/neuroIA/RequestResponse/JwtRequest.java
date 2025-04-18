package pillihuaman.com.pe.neuroIA.RequestResponse;

import java.io.Serializable;

public class JwtRequest implements Serializable {
	private static final long serialVersionUID = 5926468583005150707L;

	private String username;
	private String password;
	private String mail;

/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	//need default constructor for JSON Parsing
	public JwtRequest() {

	}

	public JwtRequest(String username, String password,String mail) {
		this.setUsername(username);
		this.setPassword(password);
		this.setMail(mail);
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}