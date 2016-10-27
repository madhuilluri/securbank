package securbank.dao;

import securbank.models.Account;
import securbank.models.Transaction;

import java.util.*;

/**
 * @author Mitikaa Sama
 *
 * Sep 26, 2016
 */

public interface TransactionDao extends BaseDao<Transaction, UUID>{
	public List<Transaction> findAll();
	public List<Transaction> findByAccount(String accountNumber);
	public List<Transaction> findByAccountAndType(Long accountNumber, String type);
	public List<Transaction> findByCriticalStatus(Boolean criticalStatus);
	public List<Transaction> findByApprovalStatus(String status);
	public List<Transaction> findPendingByAccountAndType(Account account, String type);
	public List<Transaction> findPendingByCriticalStatus(Boolean criticalStatus);
	public List<Transaction> findPendingByAccount(Long accountNumber);
	public Transaction findPendingTransactionByAccount(Long accountNumber);
	public List<Transaction> findNonCriticalByApprovalStatus(String status);
}
