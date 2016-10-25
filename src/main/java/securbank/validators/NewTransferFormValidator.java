/**
 * 
 */
package securbank.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import securbank.models.Otp;
import securbank.models.Transfer;
import securbank.models.User;
import securbank.services.OtpService;
import securbank.services.UserService;
import securbank.utils.ContraintUtils;

/**
 * @author Mitikaa
 *
 */

@Component("newTransferFormValidator")
public class NewTransferFormValidator implements Validator{

	@Autowired
	UserService userService;
	
	@Autowired
	OtpService otpService;
	
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
		return Transfer.class.equals(clazz);
	}

	/**
     * Validates initiate transfer form
     * 
     * @param target
     *            The target object
     * @param errors
     *            The errors object
     */
	@Override
	public void validate(Object target, Errors errors) {
		Transfer transfer = (Transfer) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "toAccount", "transfer.toAccount.required", "Transaction account is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "transaction.amount.required", "Transaction amount is required");
		
		if (!errors.hasFieldErrors("toAccount") && !ContraintUtils.validateTransferToAccount(transfer.getToAccount().getUser().getEmail())) {
				errors.rejectValue("toAccount", "transaction.toAccount.invalid", "Invalid Account Number");
		}
		
		if (!errors.hasFieldErrors("amount") && !ContraintUtils.validateTransactionAmount(Double.toString(transfer.getAmount()))) {
			errors.rejectValue("amount", "transaction.amount.invalid", "Invalid Amount");
		}
		
		User user = userService.getCurrentUser();
		Otp otp = otpService.getOtpByUser(user);
		if (otp==null || !transfer.getOtp().equals(otp.getCode())){
			errors.rejectValue("otp", "transfer.otp.invalid", "Invalid Otp");
		}
		
	}

}
