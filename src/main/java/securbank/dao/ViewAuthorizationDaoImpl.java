package securbank.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import securbank.models.User;
import securbank.models.ViewAuthorization;

@Repository("viewAuthorizationDao")
public class ViewAuthorizationDaoImpl extends BaseDaoImpl<ViewAuthorization, UUID> implements ViewAuthorizationDao {
	@Autowired
	EntityManager entityManager;
	
	public ViewAuthorizationDaoImpl() {
		super(ViewAuthorization.class);
	}

	/* (non-Javadoc)
	 * @see securbank.dao.ViewAuthorizationDao#findByUser(securbank.models.User)
	 */
	@Override
	public List<User> findByEmployee(User user) {
		return this.entityManager.createQuery("SELECT auth.external from ViewAuthorization auth where auth.employee = :user AND auth.active = true", User.class)
				.setParameter("user", user)
				.getResultList();
	}

	/* (non-Javadoc)
	 * @see securbank.dao.ViewAuthorizationDao#findByEmployeeAndExternal(securbank.models.User, securbank.models.User)
	 */
	@Override
	public ViewAuthorization findByEmployeeAndExternal(User employee, User external) {
		try {
			return this.entityManager.createQuery("SELECT auth from ViewAuthorization auth where auth.external = :external AND auth.employee = :employee AND auth.active = true", ViewAuthorization.class)
					.setParameter("external", external)
					.setParameter("employee", employee)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no user if found
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see securbank.dao.ViewAuthorizationDao#findInactiveByExternal(securbank.models.User)
	 */
	@Override
	public List<ViewAuthorization> findInactiveByExternal(User user) {
		return this.entityManager.createQuery("SELECT auth from ViewAuthorization auth where auth.external = :user AND auth.active = false", ViewAuthorization.class)
				.setParameter("user", user)
				.getResultList();
	}
	
	public List<ViewAuthorization> findAllByInactive() {
		return this.entityManager.createQuery("SELECT auth from ViewAuthorization auth where auth.active = false", ViewAuthorization.class)
				.getResultList();
	}
}
