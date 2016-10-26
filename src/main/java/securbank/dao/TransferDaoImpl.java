package securbank.dao;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import securbank.models.Account;
import securbank.models.Transfer;
import securbank.models.User;

/**
 * @author Mitikaa Sama
 *
 * Sep 26, 2016
 */

@Repository("transferDao")
public class TransferDaoImpl extends BaseDaoImpl<Transfer, UUID> implements TransferDao{

	@Autowired
	EntityManager entityManager;
	
	
	public TransferDaoImpl() {
		super(Transfer.class);
	}
	
	/**
     * Returns list of all transfers in the table
     * 
     * @return transfers
     */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transfer> findAll() {
		return (List<Transfer>) this.entityManager.createQuery("SELECT transfer from Transfer transfer")
				.getResultList();
	}

	/**
     * Returns list of transfers in the table filtered by from account number
     * 
     * @return transfers
     */
	@Override
	public List<Transfer> findTransferByFromAccount(Account account) {
		try {
			return this.entityManager.createQuery("SELECT transfer from Transfer transfer"
					+ " where transfer.fromAccount = :account", Transfer.class)
					.setParameter("account", account)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transfer is found
			return null;
		}
	}
	
	/**
     * Returns list of transfers in the table filtered by from account number
     * 
     * @return transfers
     */
	@Override
	public List<Transfer> findTransferByToAccount(Account toAccountnumber) {
		try {
			return this.entityManager.createQuery("SELECT transfer from Transfer transfer"
					+ " where transfer.toAccountnumber = :toAccountnumber", Transfer.class)
					.setParameter("toAccountnumber", toAccountnumber)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no trasnfer is found
			return null;
		}
	}

	@Override
	public List<Transfer> findByApprovalStatus(String status) {
		try {
			return this.entityManager.createQuery("SELECT transfer from Transfer transfer"
					+ " where transfer.status = :status", Transfer.class)
					.setParameter("status", status)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transfer is found
			return null;
		}
	}

	@Override
	public List<Transfer> findPendingTransferByFromAccount(Account fromAccount) {
		try {
			return this.entityManager.createQuery("SELECT transfer from Transfer transfer"
					+ " where transfer.fromAccount = :account AND transfer.status = :status", Transfer.class)
					.setParameter("account", fromAccount)
					.setParameter("status", "Pending")
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transfer is found
			return null;
		}
	}
	
	@Override
	public List<Transfer> findByUserAndApprovalStatus(User user, String status) {
		try {
			return this.entityManager.createQuery("SELECT transfer from Transfer transfer"
					+ " where transfer.status = :status AND (transfer.toAccount.user = :user OR transfer.fromAccount.user = :user)", Transfer.class)
					.setParameter("status", status)
					.setParameter("user", user)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transfer is found
			return null;
		}
	}

	@Override
	public List<Transfer> findNonCriticalByApprovalStatus(String approvalStatus) {
		try {
			return this.entityManager.createQuery("SELECT transfer from Transfer transfer"
					+ " where transfer.status = :status AND transfer.criticalStatus = :critical", Transfer.class)
					.setParameter("status", approvalStatus)
					.setParameter("critical", false)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transfer is found
			return null;
		}
	}
}
