package securbank.dao;

import java.util.List;
import java.util.UUID;

import securbank.models.User;
import securbank.models.ViewAuthorization;

public interface ViewAuthorizationDao extends BaseDao<ViewAuthorization, UUID> {
	public List<User> findByEmployee(User user);
	public ViewAuthorization findByEmployeeAndExternal(User employee, User external);
	public List<ViewAuthorization> findInactiveByExternal(User user);
	public List<ViewAuthorization> findAllByInactive();
}
