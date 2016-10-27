/**
 * 
 */
package securbank.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ayush Gupta
 *
 */
public class ContraintUtils {
	
	private static Pattern pattern;
	private static Matcher matcher;

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
	   + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
	private static final String ZIP_PATTERN = "[0-9]{5}";
	private static final String PHONE_PATTERN = "[0-9]{10}";
	private static final String PASSWORD_PATTERN =  "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$!%]).{6,20})";

	private static final String AMOUNT_PATTERN = "^[0-9]+(\\.[0-9]+)?$";
	private static final String ACCOUNT_PATTERN = "[0-9]+";

	private static final String INTERNAL_ROLES = "ROLE_MANAGER|ROLE_EMPLOYEE|ROLE_ADMIN";
	private static final String EXTERNAL_ROLES = "ROLE_INDIVIDUAL|ROLE_MERCHANT";
	
	private static final String SSN_PATTERN = "(?!000|666)[0-8][0-9]{2}(?!00)[0-9]{2}(?!0000)[0-9]{4}";

	/**
     * Validates username
     * 
     * @param username
     *            The username to be validated
     * @return boolean
     */
	public static boolean validateUsername(String username) {
		pattern = Pattern.compile(USERNAME_PATTERN);
		matcher = pattern.matcher(username);
		
		return matcher.matches();
	}
	
	/**
     * Validates email
     * 
     * @param email
     *            The email to be validated
     * @return boolean
     */
	public static boolean validateEmail(String email) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		
		return matcher.matches();
	}

	/**
     * Validates password
     * 
     * @param password
     *            The password to be validated
     * @return boolean
     */
	public static boolean validatePassword(String password) {
		pattern = Pattern.compile(PASSWORD_PATTERN);
		matcher = pattern.matcher(password);
		
		return matcher.matches();
	}
	
	/**
     * Validates phone
     * 
     * @param phone
     *            The phone to be validated
     * @return boolean
     */
	public static boolean validatePhone(String phone) {
		pattern = Pattern.compile(PHONE_PATTERN);
		matcher = pattern.matcher(phone);
		
		return matcher.matches();
	}
	
	/**
     * Validates zip
     * 
     * @param zip
     *            The zip to be validated
     * @return boolean
     */
	public static boolean validateZip(String zip) {
		pattern = Pattern.compile(ZIP_PATTERN);
		matcher = pattern.matcher(zip);
		
		return matcher.matches();
	}
	
	/**
	 * Validates transacitonType
	 * 
	 * @param transactionType
	 * 		The transaction type to be validated
	 * @return boolean
	 */
	public static boolean validateTransactionType(String transactionType){
		if(transactionType.equals("DEBIT") || transactionType.equals("CREDIT")) return true;
		return false;
	}
	
	/**
	 * Validates transacitonAmount
	 * 
	 * @param transacitonAmount
	 * 		The transaction amount to be validated
	 * @return boolean
	 */
	public static boolean validateTransactionAmount(String transactionAmount){
		pattern = Pattern.compile(AMOUNT_PATTERN);
		matcher = pattern.matcher(transactionAmount);
		
		return matcher.matches();
	}
	
	/**
	 * Validates Account
	 * 
	 * @param toAccount
	 * 		Validates the account for transfer
	 * @return boolean
	 */
	public static boolean validateTransferToAccount(String transferAccountEmail){
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(transferAccountEmail);
		
		return matcher.matches();
	}

     /** Validates internal role
     * 
     * @param role
     *            The role to be validated
     * @return boolean
     */
	public static boolean validateInternalRole(String role) {
		pattern = Pattern.compile(INTERNAL_ROLES);
		matcher = pattern.matcher(role);
		
		return matcher.matches();
	}
	
	/**
     * Validates external role
     * 
     * @param role
     *            The role to be validated
     * @return boolean
     */
	public static boolean validateExternalRole(String role) {
		pattern = Pattern.compile(EXTERNAL_ROLES);
		matcher = pattern.matcher(role);
		
		return matcher.matches();
	}
	
	/**
     * Validates ssn
     * 
     * @param ssn
     *            The ssn to be validated
     * @return boolean
     */
	public static boolean validateSSN(String ssn) {
		pattern = Pattern.compile(SSN_PATTERN);
		matcher = pattern.matcher(ssn);
		
		return matcher.matches();
	}
}
