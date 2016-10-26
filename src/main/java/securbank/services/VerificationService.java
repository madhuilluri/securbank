/**
 * 
 */
package securbank.services;

import java.util.UUID;

import securbank.models.User;
import securbank.models.Verification;

/**
 * @author Ayush Gupta
 *
 */
public interface VerificationService {
	public User getUserByIdAndType(UUID id, String type);
	public Verification createVerificationCodeByType(User user, String type);
	public void removeVerification(UUID id);
}
