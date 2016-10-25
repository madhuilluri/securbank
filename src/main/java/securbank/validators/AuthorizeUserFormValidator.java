package securbank.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import securbank.models.User;
import securbank.models.ViewAuthorization;
import securbank.services.ViewAuthorizationService;
/**
 * @author Ayush Gupta
 *
 */
@Component("authorizeUserFormValidator")
public class AuthorizeUserFormValidator implements Validator{

	@Autowired
	private ViewAuthorizationService viewAuthorizationService;
	
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
		return ViewAuthorization.class.equals(clazz);
	}

	/**
     * Validates create user form
     * 
     * @param target
     *            The target object
     * @param errors
     *            The errors object
     */
	@Override
	public void validate(Object target, Errors errors) {
		ViewAuthorization request = (ViewAuthorization) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "employee.email", "employee.email.required", "Employee's email is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "external.email", "external.email.required", "External User's email is required");
		User external = request.getExternal();
		User employee = request.getEmployee();
		if (!errors.hasFieldErrors("employee.email") && employee == null) {
			 errors.rejectValue("employee.email", "employee.invalid", "Invalid Email");
		}
		if (!errors.hasFieldErrors("external.email") && external == null) {
			 errors.rejectValue("external.email", "email.invalid", "Invalid Email");
		}
		if (!errors.hasFieldErrors("external.email") && !external.getType().equals("external")) {
			 errors.rejectValue("external.email", "email.invalid", "User must be external");
		}
		if (!errors.hasFieldErrors("employee.email") && !employee.getRole().equalsIgnoreCase("ROLE_EMPLOYEE")) {
			 errors.rejectValue("employee.email", "email.invalid", "User must be an employee");
		}
		if (!errors.hasErrors() && viewAuthorizationService.hasAccess(employee, external)) {
			errors.rejectValue("employee.email", "email.invalid", "Employee already has an access");
		}
		
	}
}
