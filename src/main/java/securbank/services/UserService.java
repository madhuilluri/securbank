/**
 * 
 */
package securbank.services;

import java.util.List;
import java.util.UUID;

import securbank.models.ModificationRequest;
import securbank.models.NewUserRequest;
import securbank.models.User;

/**
 * @author Ayush Gupta
 *
 */
public interface UserService {
	public User createExternalUser(User user);
	public User createInternalUser(User user);
	public NewUserRequest createNewUserRequest(NewUserRequest newUserRequest);
	public User getCurrentUser();
	public User editUser(User user);
	public void deleteUser(UUID id);
	public boolean verifyNewUser(UUID userId);
	public NewUserRequest getNewUserRequest(UUID newUserRequestId);
	public List<User> getUsersByType(String type);
	public User getUserByIdAndActive(UUID id);
	public ModificationRequest createInternalModificationRequest(User user);
	public ModificationRequest createExternalModificationRequest(User user);
	public ModificationRequest approveModificationRequest(ModificationRequest request);
	public ModificationRequest rejectModificationRequest(ModificationRequest requestId);
	public List<ModificationRequest> getModificationRequests(String status, String type);
	public ModificationRequest getModificationRequest(UUID requestId);
	public boolean verifyModificationRequest(String status, UUID requestId);
	public boolean verifyModificationRequestUserType(UUID requestId, String type);
	public boolean verifyUserType(UUID id, String type);
	public void deleteModificationRequest(ModificationRequest request);
}
