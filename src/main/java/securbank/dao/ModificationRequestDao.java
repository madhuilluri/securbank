package securbank.dao;

import securbank.models.ModificationRequest;
import securbank.models.User;

import java.util.List;
import java.util.UUID;

/**
 * @author Ayush Gupta
 *
 */
public interface ModificationRequestDao extends BaseDao<ModificationRequest, UUID> {
	public List<ModificationRequest> findAll();
	public List<ModificationRequest> findAllbyStatusAndUserType(String status, String userType);
	public List<ModificationRequest> findAllbyUser(User user);
	public List<ModificationRequest> findAllbyStatusAndUserTypeAndUsers(String status, String userType, List<User> users);
}
