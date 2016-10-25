package securbank.dao;

import java.util.List;
import java.util.UUID;

import securbank.models.User;
import securbank.models.Verification;

/**
 * 
 * @author Ayush Gupta
 *
 */
public interface VerificationDao extends BaseDao<Verification, UUID> {
	public User findUserByIdAndType(UUID id, String type);
	public List<Verification> findAllByUserAndType(User user, String type);
}