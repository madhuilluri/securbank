package securbank.services;

import securbank.models.User;

/**
 * @author Ayush Gupta
 *
 */
public interface AuthenticationService {
	public User verifyUser(String username, String password);
	public User updateLoginTime(User user);
	public String getRedirectUrlFromRole(String role);
}
