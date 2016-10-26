package securbank.dao;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import securbank.models.ModificationRequest;
import securbank.models.User;

/**
 * @author Ayush Gupta
 *
 */
@Repository("modificationRequestDao")
public class ModificationRequestDaoImpl extends BaseDaoImpl<ModificationRequest, UUID> implements ModificationRequestDao {
	
	@Autowired
	EntityManager entityManager; 
	
	public ModificationRequestDaoImpl() {
		super(ModificationRequest.class);
	}
	
	/**
     * Returns list of all requests in the tables for status
     *  
     * @return modificationRequests
     */
	@Override
	public List<ModificationRequest> findAll() {
		return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.active = TRUE", ModificationRequest.class)
									.getResultList();
	}

	/**
     * Returns list of all request in the tables
     * @param status
     * 			Status for which to query db
     * @param userType
     * 			type of user(internal or external) for which to query db
     * 
     * @return modificationRequests
     */
	@Override
	public List<ModificationRequest> findAllbyStatusAndUserType(String status, String userType) {
		return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.status = :status AND userType = :type AND request.active = TRUE", ModificationRequest.class)
				.setParameter("status", status)							
				.setParameter("type", userType)							
				.getResultList();
	}	
	
	/**
     * Returns request for the given Id.
     * 
     * @param id
     *            The id to query db
     * @return User
     */
	@Override
	public ModificationRequest findById(UUID id) {
		try {
			return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.modificationRequestId = :id AND request.active = TRUE", ModificationRequest.class)
					.setParameter("id", id)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no user if found
			return null;
		}
	}
	
	/**
     * Returns list of all active requests in the tables for a user
     *  
     * @return modificationRequests
     */
	@Override
	public List<ModificationRequest> findAllbyUser(User user) {
		return this.entityManager.createQuery("SELECT request from ModificationRequest request where request.user = :user AND request.active = TRUE", ModificationRequest.class)
				.setParameter("user", user)							
				.getResultList();
	}

	/* (non-Javadoc)
	 * @see securbank.dao.ModificationRequestDao#findAllbyStatusAndUserTypeAndUsers(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public List<ModificationRequest> findAllbyStatusAndUserTypeAndUsers(String status, String userType,
			List<User> users) {
		return this.entityManager.createQuery("SELECT request from ModificationRequest request " +
									"WHERE request.status = :status " +
									"AND userType = :type " +
									"AND request.active = TRUE " +
									"AND request.user IN :users", ModificationRequest.class)
				.setParameter("status", status)							
				.setParameter("type", userType)
				.setParameter("users", users)
				.getResultList();
	}
}
