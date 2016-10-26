package securbank.dao;

import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import securbank.models.User;

/**
 * @author Ayush Gupta
 *
 */
@Repository("userDao")
public class UserDaoImpl extends BaseDaoImpl<User, UUID> implements UserDao {
	@Autowired
	EntityManager entityManager; 
	
	public UserDaoImpl() {
		super(User.class);
	}
	
	/**
     * Returns user for the given Id.
     * 
     * @param user
     *            The username or email to query db
     * @return User
     */
	@Override
	public User findByUsernameOrEmail(String user) {
		try {
			return (User) this.entityManager.createQuery("SELECT user from User user where (user.username = :username OR user.email = :email) AND user.active = true")
					.setParameter("username", user)
					.setParameter("email", user)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no user if found
			return null;
		}
		catch(NonUniqueResultException e) {
			// returns null if no user if found
			return null;
		}
	}


	/**
     * Returns user for the given Id.
     * 
     * @param user
     *            The username or email to query db
     * @return User
     */
	@Override
	public User findByUsername(String username) {
		try {
			return this.entityManager.createQuery("SELECT user from User user where user.username = :username AND user.active = true", User.class)
					.setParameter("username", username)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no user if found
			return null;
		}
		catch(NonUniqueResultException e) {
			// returns null if no user if found
			return null;
		}
	}
	
	/**
     * Returns list of all users in the tables
     * 
     * @return users
     */
	@Override
	public List<User> findAll() {
		return this.entityManager.createQuery("SELECT user from User user", User.class)
											.getResultList();
	}
	
	/**
     * Returns list of all users by type
     * @param type
     * 			The type of the user to be retrieved
     * @return users
     */
	public List<User> findAllByType(String type) {
		return this.entityManager.createQuery("SELECT user from User user where user.type = :type AND user.active = true", User.class)
				.setParameter("type", type)
				.getResultList();
	}
	
	/**
     * Returns if username exists in table
     * 
     * @param username
     * 			The username to check
     * @return boolean
     */
	@Override
	public boolean usernameExists(String username) {
		return this.entityManager.createQuery("SELECT COUNT(user) from User user where (user.username = :username AND user.active = true)", Long.class)
					.setParameter("username", username)
					.getSingleResult() != 0;
	}
	
	/**
     * Returns if email exists in table
     * 
     * @param email
     * 			The email to check
     * @return boolean
     */
	@Override
	public boolean emailExists(String email) {
		return this.entityManager.createQuery("SELECT COUNT(user) from User user where (user.email = :email AND user.active = true)", Long.class)
					.setParameter("email", email)
					.getSingleResult() != 0;
	}
	
	/**
     * Returns if phone exists in table
     * 
     * @param phone
     * 			The phone to check
     * @return boolean
     */
	@Override
	public boolean phoneExists(String phone) {
		return this.entityManager.createQuery("SELECT COUNT(user) from User user where (user.phone = :phone AND user.active = true)", Long.class)
					.setParameter("phone", phone)
					.getSingleResult() != 0;
	}
	
	/**
	 * Returns if ssn exists in table
	 * 
	 * @param ssn
	 * 			The ssn to check
	 * @return boolean
	 */
	@Override
	public boolean ssnExists(String ssn) {
		return this.entityManager.createQuery("SELECT COUNT(pii) from Pii pii where (pii.ssn = :ssn)", Long.class)
				.setParameter("ssn",ssn)
				.getSingleResult()!=0;

	}

	@Override
	public List<User> accessPii() {
		return this.entityManager.createQuery("SELECT user from User user where user.active=true", User.class)
											.getResultList();
	}
}
