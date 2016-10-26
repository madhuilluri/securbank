/**
 * 
 */
package securbank.services;

import javax.transaction.Transactional;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import securbank.dao.AccountDao;
import securbank.models.Account;

/**
 * @author Ayush Gupta
 *
 */
@Service("accountService")
@Transactional
public class AccountServiceImpl implements AccountService {
	
	@Autowired
	AccountDao accountDao;
	
	/* Creates new checking account for the user and type
	 * @param account
	 * 			account to be created
	 * @return account
	 */
	@Override
	public Account createAccount(Account account) {
		account.setCreatedOn(LocalDateTime.now());
		account.setActive(true);
		
		return accountDao.save(account);
	}

	@Override
	public boolean accountExists(Account account) {
		return accountDao.accountExists(account);
	}
}
