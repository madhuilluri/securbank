package securbank.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Entity
@Component("forgotPasswordRequest")
public class ForgotPasswordRequest {
	
	@Id
	@NotNull
	String username;
	
	@NotNull
	String email;
	
	public ForgotPasswordRequest(){
		
	}
	
	/**
	 * 
	 * @param userName
	 * @param email
	 */
	public ForgotPasswordRequest(String username, String email){
		super();
		this.username = username;
		this.email = email;
	}

	/**
	 * @return the userName
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
