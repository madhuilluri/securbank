package securbank.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

/**
 * 
 * @author Madhu
 *
 */

@Component("createPasswordRequest")
public class CreatePasswordRequest {
	
	@Id
	@NotNull
	private String phone;

	@NotNull
	private String email;
		
	@NotNull
	private String newPassword;
	
	@NotNull
	private String confirmPassword;
	
	public CreatePasswordRequest(){
		
	}
	/**
	 * @param userId
	 * @param existingPassword
	 * @param newPassword
	 * @param confirmPassword
	 */
	public CreatePasswordRequest(String phone, String email,String newPassword, String confirmPassword){
		super();
		this.email = email;
		this.phone = phone;
		this.newPassword = newPassword;
		this.confirmPassword = confirmPassword;
	}
	
	/**
	 * 
	 * @return phone 
	 */
	public String getPhone() {
		return phone;
	}
	
	/**
	 * 
	 * @param phone phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	/**
	 * 
	 * @return email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * 
	 * @param email email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * 
	 * @return newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}
	
	/**
	 * 
	 * @param newPassword newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	/**
	 * 
	 * @return confirmPassword
	 */
	public String getConfirmPassword() {
		return confirmPassword;
	}
	
	/**
	 * 
	 * @param confirmPassword confirmPassword to set
	 */
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	


}