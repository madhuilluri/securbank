/**
 * 
 */
package securbank.services;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import securbank.dao.ViewAuthorizationDao;
import securbank.models.User;
import securbank.models.ViewAuthorization;

/**
 * @author Ayush Gupta
 *
 */
@Service("viewAuthorizationService")
@Transactional
public class ViewAuthorizationServiceImpl implements ViewAuthorizationService {
	
	@Autowired
	ViewAuthorizationDao viewAuthorizationDao;
	
	@Autowired
	UserService userService;
	
	/* (non-Javadoc)
	 * @see securbank.services.ViewAuthorizationService#hasAccess(securbank.models.User, securbank.models.User)
	 */
	@Override
	public boolean hasAccess(User employee, User external) {
		ViewAuthorization authorization = viewAuthorizationDao.findByEmployeeAndExternal(employee, external);
		if (authorization == null) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see securbank.services.ViewAuthorizationService#createAuthorization(securbank.models.ViewAuthorization)
	 */
	@Override
	public ViewAuthorization createAuthorization(User employee, User external, Boolean active) {
		ViewAuthorization authorization = new ViewAuthorization();
		authorization.setEmployee(employee);
		authorization.setExternal(external);
		authorization.setActive(active);
		authorization = viewAuthorizationDao.save(authorization);
		
		return authorization;
	}

	/* (non-Javadoc)
	 * @see securbank.services.ViewAuthorizationService#getAllAuthorization(securbank.models.User)
	 */
	@Override
	public List<User> getAllAuthorization(User user) {
		return viewAuthorizationDao.findByEmployee(user);
	}

	/* (non-Javadoc)
	 * @see securbank.services.ViewAuthorizationService#approveAuthorization(java.util.UUID)
	 */
	@Override
	public ViewAuthorization approveAuthorization(ViewAuthorization authorization) {
		if (authorization == null) {
			return null;
		}
		if (authorization.getActive() == true) {
			return authorization;
		}
		if (authorization.getStatus().equalsIgnoreCase("approved")) {
			authorization.setActive(true);
			authorization = viewAuthorizationDao.update(authorization);
		}
		else {
			viewAuthorizationDao.remove(authorization);
		}
		
		return authorization;
	}

	/* (non-Javadoc)
	 * @see securbank.services.ViewAuthorizationService#getAuthorizationById(java.util.UUID)
	 */
	@Override
	public ViewAuthorization getAuthorizationById(UUID id) {
		return viewAuthorizationDao.findById(id);
	}

	/* (non-Javadoc)
	 * @see securbank.services.ViewAuthorizationService#getPendingAuthorization(securbank.models.User)
	 */
	@Override
	public List<ViewAuthorization> getPendingAuthorization(User user) {
		return viewAuthorizationDao.findInactiveByExternal(user);
	}
	
	public List<ViewAuthorization> getPendingAuthorization() {
		return viewAuthorizationDao.findAllByInactive();
	}
}
