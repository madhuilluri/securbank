package securbank.dao;

import securbank.models.Account;
import securbank.models.User;

import java.util.*;

/**
 * 
 * @author Madhu Illuri
 *
 */
public interface AccountDao extends BaseDao<Account, UUID> {
	public List<Account> findAll();
	public boolean accountExistsbyType(User user, String type);
	public boolean updateBalance();
	public boolean accountExists(Account account);
}