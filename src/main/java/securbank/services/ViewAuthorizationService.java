/**
 * 
 */
package securbank.services;

import java.util.List;
import java.util.UUID;

import securbank.models.User;
import securbank.models.ViewAuthorization;

/**
 * @author Ayush Gupta
 *
 */
public interface ViewAuthorizationService {
	public boolean hasAccess(User employee, User external);
	public ViewAuthorization createAuthorization(User employee, User external, Boolean active);
	public List<User> getAllAuthorization(User user);
	public ViewAuthorization approveAuthorization(ViewAuthorization authorization);
	public ViewAuthorization getAuthorizationById(UUID id);
	public List<ViewAuthorization> getPendingAuthorization(User user);
	public List<ViewAuthorization> getPendingAuthorization();
}
