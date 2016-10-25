package securbank.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

@Component("forgotPasswordRequest")
public class ForgotPasswordRequest {
	
	@Id
	@NotNull
	String userName;
	
	@NotNull
	String email;
	
	public ForgotPasswordRequest(){
		
	}
	
	/**
	 * 
	 * @param userName
	 * @param email
	 */
	public ForgotPasswordRequest(String userName, String email){
		super();
		this.userName = userName;
		this.email = email;
	}

	/**
	 * @return the userName
	 */
	public String getuserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setuserName(String userName) {
		this.userName = userName;
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
