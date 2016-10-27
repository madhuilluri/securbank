package securbank.services;

import javax.servlet.http.Cookie;

import securbank.models.User;

/**
 * @author Ayush Gupta
 *
 */
public interface AuthenticationService {
	public User verifyUser(String username, String password);
	public User updateLoginTime(User user);
	public String getRedirectUrlFromRole(String role);
	public Cookie validateCookie(Cookie[] cookies, String username);
}
