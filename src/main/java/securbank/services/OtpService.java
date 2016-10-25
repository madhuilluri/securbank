package securbank.services;

import securbank.models.Otp;
import securbank.models.User;

/**
 * @author Mitikaa
 *
 */

public interface OtpService {
	public Otp createOtpForUser(User user);
	public Otp getOtpByUser(User user);
	public void deactivateOtpByUser(User user);
}
