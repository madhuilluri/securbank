package securbank.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import securbank.models.ForgotPasswordRequest;
import securbank.utils.ContraintUtils;

import org.springframework.validation.ValidationUtils;



/**
 * @author Madhu
 *
 */

@Component("forgotPasswordRequestFormValidator")
public class ForgotPasswordRequestFormValidator implements Validator{
	
	/**
     * If supports class
     * 
     * @param clazz
     *            The class to check
     *            
     * @return boolean
     */	
	@Override
	public boolean supports(Class<?> clazz) {
		return ForgotPasswordRequest.class.equals(clazz);
	}

	/**
	 * Validates forgot password request form 
	 * 
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(Object target, Errors errors) {
		ForgotPasswordRequest forgotPasswordRequest = (ForgotPasswordRequest) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "user.username.required", "Username is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "forgotPasswordRequest.email.required", "Email is required");
		
		if (!errors.hasFieldErrors("email")) {
			if (!ContraintUtils.validateEmail(forgotPasswordRequest.getEmail())) {
				errors.rejectValue("email", "forgotPasswordRequest.email.contraint", "Invalid Email");
			}
		}
		
		if (!errors.hasFieldErrors("username")) {
			if (!ContraintUtils.validateUsername(forgotPasswordRequest.getuserName())) {
				errors.rejectValue("username", "forgotPasswordRequest.username.contraint", "Username can contain lowercase alphanumeric with (-,_) and length should be between 3 and 15");
			}
		}
		

	}

}