package securbank.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import securbank.dao.UserDao;
import securbank.models.Pii;
import securbank.models.User;

@Service("accessPii")
public class AccessPiiServiceImpl implements AccessPiiService{

	@Autowired
	UserDao userDao;
	
	@Autowired
	UserService userService;
	
	@Override
	public List<Pii> ListAllPII() {
		List<Pii> allPii = userDao.accessPii();
		return allPii;
	}

}
