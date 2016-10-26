package securbank.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.ValidationUtils;

import securbank.models.Pii;
import securbank.models.User;
import securbank.utils.ContraintUtils;
import securbank.dao.UserDao;

/**
 * @author Madhu
 *
 */
@Component("ssnFormValidator")
public class SsnFormValidator implements Validator{

	@Autowired
	private UserDao userDao;
	
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
		return User.class.equals(clazz);
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
		Pii pii = (Pii) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ssn", "pii.ssn.required", "SSN is required");
		
		if (!errors.hasFieldErrors("ssn")) {
			if (!ContraintUtils.validateSSN(pii.getSsn())) {
				errors.rejectValue("ssn", "pii.ssn.contraint", "Invalid SSN");
			}
			else if (userDao.ssnExists(pii.getSsn())) {
				errors.rejectValue("ssn", "pii.ssn.exists", "SSN exists");
			}
		}
		
	} 
}
