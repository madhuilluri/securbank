package securbank.services;

import java.util.List;
import java.util.UUID;

import securbank.models.Transaction;
import securbank.models.Transfer;

/**
 * @author Mitikaa
 *
 */
public interface TransactionService {
	public Transaction initiateDebit(Transaction transaction);
	public Transaction initiateCredit(Transaction transaction);
	public Transaction initiateTransfer(Transfer transfer);
	public Transaction approveTransaction(Transaction transaction);
	public Transfer approveTransfer(Transfer transfer);
	public Transaction declineTransaction(Transaction transaction);
	public Transaction declineTransaction(Transfer transfer);
	public List<Transaction> getPendingTransactionsByAccountNumber(Long accountNumber);
	public List<Transaction> getPendingTransactionsByType(Long accountNumber, String accountType);
	public List<Transaction> getTransactionsByStatus(String approvalStatus);
	public Transaction getTransactionById(UUID id);
	public Transaction getPendingTransactionByAccountNumber(Long accountNumber);
	public Transaction approveTransactionFromTransfer(Transaction transaction);
	public boolean isTransactionValid(Transaction transaction);
}
