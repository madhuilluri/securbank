package securbank.dao;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import securbank.models.Account;
import securbank.models.Transaction;

/**
 * @author Mitikaa Sama
 *
 * Sep 26, 2016
 */

@Repository("transactionDao")
public class TransactionDaoImpl extends BaseDaoImpl<Transaction, UUID> implements TransactionDao{
	@Autowired
	EntityManager entityManager;
	
	
	public TransactionDaoImpl() {
		super(Transaction.class);
	}
	
	/**
     * Returns list of all transactions in the table
     * 
     * @return transactions
     */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transaction> findAll() {
		return (List<Transaction>) this.entityManager.createQuery("SELECT transaction from Transaction transaction")
				.getResultList();
	}
	
	/**
     * Returns list of transactions in the table filtered by account number
     * 
     * @return transactions
     */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transaction> findByAccount(String accountNumber) {
		try {
			return (List<Transaction>) this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.accountNumber = :accountNumber")
					.setParameter("accountNumber", accountNumber)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	
	/**
     * Returns list of transactions in the table filtered by account number and type
     * 
     * @return transactions
     */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transaction> findByAccountAndType(Long accountNumber, String type) {
		try {
			return (List<Transaction>) this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where (transaction.accountNumber = :accountNumber AND transaction.type = type)")
					.setParameter("accountNumber", accountNumber)
					.setParameter("type", type)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	/**
     * Returns list of transactions in the table filtered by critical status of the transaction
     * 
     * @return transactions
     */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transaction> findByCriticalStatus(Boolean criticalStatus) {
		try {
			return (List<Transaction>) this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.criticalStatus = :criticalStatus")
					.setParameter("criticalStatus", criticalStatus)
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	/**
	 Returns list of transactions in the table filtered by the approval status of the transaction
     * 
     * @return transactions
	 */
	@Override
	public List<Transaction> findByApprovalStatus(String approvalStatus) {
		try {
			return this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.approvalStatus = :approvalStatus", Transaction.class)
					.setParameter("approvalStatus", approvalStatus)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}
	
	/**
     * Returns list of transactions in the table filtered by account number and type
     * 
     * @return transactions
     */
	@Override
	public List<Transaction> findPendingByAccountAndType(Account account, String type) {
		try {
			return this.entityManager.createQuery("SELECT transaction from Transaction transaction "
					+ "where transaction.account = :account AND transaction.type = :type AND transaction.approvalStatus = :approvalStatus", Transaction.class)
					.setParameter("account", account)
					.setParameter("type", type)
					.setParameter("approvalStatus", "Pending")
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	/**
     * Returns list of transactions in the table filtered by critical status of the transaction
     * 
     * @return transactions
     */
	@Override
	public List<Transaction> findPendingByCriticalStatus(Boolean criticalStatus) {
		try {
			return  this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.criticalStatus = :criticalStatus", Transaction.class)
					.setParameter("criticalStatus", criticalStatus)
					.setParameter("approvalStatus", "Pending")
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	/**
     * Returns list of transactions in the table filtered by account number
     * 
     * @return transactions
     */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transaction> findPendingByAccount(Long accountNumber) {
		try {
			return (List<Transaction>) this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.accountNumber = :accountNumber AND transaction.type = type AND approvalStatus = :approvalStatus")
					.setParameter("accountNumber", accountNumber)
					.setParameter("approvalStatus", "Pending")
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	@Override
	public Transaction findPendingTransactionByAccount(Long accountNumber) {
		try {
			System.out.println("Inside findPendingTransactionByAccount");
			return  this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.account.accountNumber = :accountNumber AND transaction.approvalStatus = :approvalStatus", Transaction.class)
					.setParameter("accountNumber", accountNumber)
					.setParameter("approvalStatus", "Pending")
					.getSingleResult();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}

	@Override
	public List<Transaction> findNonCriticalByApprovalStatus(String status) {
		try {
			return this.entityManager.createQuery("SELECT transaction from Transaction transaction"
					+ " where transaction.approvalStatus = :approvalStatus AND transaction.criticalStatus = :criticalStatus", Transaction.class)
					.setParameter("approvalStatus", status)
					.setParameter("criticalStatus", false)
					.getResultList();
		}
		catch(NoResultException e) {
			// returns null if no transaction is found
			return null;
		}
	}
}
