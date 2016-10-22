/**
 * 
 */
package securbank.services;

import org.springframework.mail.SimpleMailMessage;

/**
 * @author Ayush Gupta
 *
 */
public interface EmailService {
	public boolean sendEmail(SimpleMailMessage message);
}
