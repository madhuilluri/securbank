/**
 * 
 */
package securbank.services;

import securbank.models.Account;

/**
 * @author Ayush Gupta
 *
 */
public interface AccountService {
	public Account createAccount(Account account);
	public boolean accountExists(Account account);
}
