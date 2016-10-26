package securbank.dao;

import securbank.models.User;

import java.util.List;
import java.util.UUID;

/**
 * @author Ayush Gupta
 *
 */
public interface UserDao extends BaseDao<User, UUID> {
	public List<User> findAll();
	public List<User> findAllByType(String type);
	public User findByUsernameOrEmail(String user);
	public User findByUsername(String username);
	public boolean usernameExists(String username);
	public boolean emailExists(String email);
	public boolean phoneExists(String phone);
	public boolean ssnExists(String ssn);
	public List<User> accessPii();
}
