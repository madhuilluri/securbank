package securbank.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import securbank.models.CreatePasswordRequest;
import securbank.utils.ContraintUtils;

import org.springframework.validation.ValidationUtils;


/**
 * @author Madhu
 *
 */
@Component("createPasswordFormValidator")
public class CreatePasswordFormValidator implements Validator{
	
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
		return CreatePasswordRequest.class.equals(clazz);
	}

	/**
	 * Validates create password request form 
	 * 
	 * @param target
	 * @param errors
	 */
	@Override
	public void validate(Object target, Errors errors) {
		CreatePasswordRequest createPasswordRequest = (CreatePasswordRequest) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "createPasswordRequest.email.required", "Email is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "createPasswordRequest.phone.required", "Phone Number is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "createPasswordRequest.newPassword.required", "New Password is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "createPasswordRequest.confirmPassword.required", "Confirm Password is required");
		
		if (!errors.hasFieldErrors("email")) {
			if (!ContraintUtils.validateEmail(createPasswordRequest.getEmail())) {
				errors.rejectValue("email", "createPasswordRequest.email.contraint", "Invalid Email");
			}
		}
		
		if (!errors.hasFieldErrors("phone")) {
			if (!ContraintUtils.validatePhone(createPasswordRequest.getPhone())) {
				errors.rejectValue("phone", "createPasswordRequest.phone.contraint", "Invalid Phone");
			}
		}
		
		if (!errors.hasFieldErrors("newPassword")) {
			if (!ContraintUtils.validatePassword(createPasswordRequest.getNewPassword())) {
				errors.rejectValue("newPassword", "createPasswordRequest.newPassword.contraint", "Password should contain one letter, number and special character. Length of password should be between 6 and 20");
			}
			else if (!createPasswordRequest.getNewPassword().equals(createPasswordRequest.getConfirmPassword())) {
				errors.rejectValue("newPassword", "createPasswordRequest.newPassword.match", "Password doesn't match");
			}
		}
	}

}