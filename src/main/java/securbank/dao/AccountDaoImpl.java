package securbank.dao;

import securbank.models.Account;
import securbank.models.User;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;


/**
 * 
 * @author Madhu Illuri
 *
 */
@Repository("accountDao")
public class AccountDaoImpl extends BaseDaoImpl<Account, UUID> implements AccountDao {
	@Autowired
	EntityManager entityManager;
	
	public AccountDaoImpl(){
		super(Account.class);
	}
	
	/**
	 * function to return all the accounts 
	 * @return accounts 
	 */
	@Override
	public List<Account> findAll(){
		return this.entityManager.createQuery("SELECT acc from Account acc", Account.class).getResultList();					
	}
	
	/**
     * Returns if account exists for user
     *  
     * @param user
     * 			The user for which account to check
     * @param type
     * 			The type of account to check 
     * @return boolean
     */
	@Override
	public boolean accountExistsbyType(User user, String type) {
		return this.entityManager.createQuery("SELECT COUNT(account) from Account account where (account.user = :user AND account.type = :type AND account.active = true)", Long.class)
				.setParameter("user", user)
				.setParameter("type", type)
				.getSingleResult() != 0;
	}

	
	
	@Override
	public boolean updateBalance() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean accountExists(Account account) {
		return this.entityManager.createQuery("SELECT COUNT(account) from Account account where (account.accountNumber = :accountNumber)", Long.class)
				.setParameter("accountNumber", account.getAccountNumber())
				.getSingleResult() != 0;
	}
	
}
