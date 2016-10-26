package securbank.services;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import securbank.dao.AccountDao;
import securbank.dao.TransactionDao;
import securbank.dao.TransferDao;
import securbank.models.Account;
import securbank.models.Transaction;
import securbank.models.Transfer;
import securbank.models.User;

/**
 * @author Mitikaa
 *
 */

@Service("transactionService")
@Transactional
public class TransactionServiceImpl implements TransactionService{

	@Autowired
	private TransactionDao transactionDao;
	
	@Autowired
	private TransferDao transferDao;
	
	@Autowired
	private AccountDao accountDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private Environment env;
	
	@Autowired
	OtpService otpService;
	
	private SimpleMailMessage message;
	
	final static Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

	/**
     * Initiates new debit transaction
     * 
     * @param transaction
     *            The transaction to be initiated
     * @return transaction
     */
	@Override
	public Transaction initiateDebit(Transaction transaction) {
		logger.info("Initiating new debit request");
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		
		//get user's checking account
		logger.info("Getting current user's checking account");
		for (Account acc: currentUser.getAccounts()){
			if (acc.getType().equalsIgnoreCase("checking")){
				transaction.setAccount(acc);
			}
		}
		
		//check if debit transaction is valid
		if(isTransactionValid(transaction) == false){
			return null;
		}
		
		transaction.setApprovalStatus("Pending");
		if (transaction.getAmount() > Double.parseDouble(env.getProperty("critical.amount"))) {
			transaction.setCriticalStatus(true);
		}

		transaction.setType("DEBIT");
		transaction.setCreatedOn(LocalDateTime.now());
		transaction.setActive(true);
		transaction = transactionDao.save(transaction);
		logger.info("After TransactionDao save");
		
		//send email to user
		User user = transaction.getAccount().getUser();
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.transaction.debit.body"));
		message.setSubject(env.getProperty("external.user.transaction.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		return transaction;
	}

	/**
     * Initiates new credit transaction
     * 
     * @param transaction
     *            The transaction to be initiated
     * @return transaction
     * */
	@Override
	public Transaction initiateCredit(Transaction transaction) {
		logger.info("Initiating new credit request");
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		
		//get user's checking account 
		logger.info("Getting current user's checking account");
		for (Account acc: userService.getCurrentUser().getAccounts()){
			if (acc.getType().equalsIgnoreCase("checking")){
				transaction.setAccount(acc);
			}
		}
		
		
		transaction.setApprovalStatus("Pending");
		if (transaction.getAmount() > Double.parseDouble(env.getProperty("critical.amount"))) {
			transaction.setCriticalStatus(true);
		}
		
		transaction.setType("CREDIT");
		transaction.setCreatedOn(LocalDateTime.now());
		transaction.setActive(true);
		transaction = transactionDao.save(transaction);
		logger.info("After TransactionDao save");

		//send email to user
		User user = transaction.getAccount().getUser();
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.transaction.credit.body"));
		message.setSubject(env.getProperty("external.user.transaction.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		return transaction;
	}

	/**
     * Initiates new transfer transaction
     * 
     * @param transfer
     *            The transfer to be initiated
     * @return transaction
     * */
	@Override
	public Transaction initiateTransfer(Transfer transfer) {
		logger.info("Initiating new transfer request");
		return null;
	}

	/**
     * Approves new transaction
     * 
     * @param transaction
     *            The transaction to be approved
     * @return transaction
     * */
	@Override
	public Transaction approveTransaction(Transaction transaction) {
		logger.info("Inside approve transaction");
		
		//check if transaction is pending
		if(transactionDao.findById(transaction.getTransactionId())==null){
			logger.info("Not a pending transaction");
			return null;
		}
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
	    
		Account account = transaction.getAccount();
		Double oldBalance = account.getBalance();
		Double newBalance = 0.0;
		if(transaction.getType().equalsIgnoreCase("DEBIT")){
			newBalance = oldBalance - transaction.getAmount();
		}
		else {
			newBalance = oldBalance + transaction.getAmount();
		}
		transaction.setApprovalStatus("Approved");
		transaction.setOldBalance(oldBalance);
		transaction.setNewBalance(newBalance);
		account.setBalance(newBalance);
		transaction.setModifiedOn(LocalDateTime.now());
		transaction.setModifiedBy(currentUser);
		transaction.setActive(true);
		transaction = transactionDao.update(transaction);
		account = accountDao.update(account);
		return transaction;
	}

	/**
     * Approves new transfer
     * 
     * @param transfer
     *            The transfer to be approved
     * @return transaction
     * */
	@Override
	public Transfer approveTransfer(Transfer transfer) {
		logger.info("Approving transfer request");
		
		Account toAccount = transfer.getToAccount();
		Account fromAccount = transfer.getFromAccount();
		
		//initiate transaction for credit in toAccount and approve
		Transaction transactionFrom = new Transaction();
		Transaction transactionTo = new Transaction();
		
		transactionTo.setAccount(toAccount);
		transactionTo.setAmount(transfer.getAmount());
		transactionTo.setActive(true);
		transactionTo.setApprovalStatus("Pending");
		transactionTo.setType("CREDIT");
		transactionTo.setTransfer(transfer);
		transactionTo = initiateCredit(transactionTo);
		approveTransactionFromTransfer(transactionTo);
		
		//initiate transaction for debit in fromAccount and approve
		transactionFrom.setAccount(fromAccount);
		transactionFrom.setAmount(transfer.getAmount());
		transactionFrom.setActive(true);
		transactionFrom.setApprovalStatus("Pending");
		transactionFrom.setType("DEBIT");
		transactionFrom.setTransfer(transfer);
		transactionFrom = initiateDebit(transactionFrom);
		approveTransactionFromTransfer(transactionFrom);
		
		return transfer;
	}

	/**
     * Declines new transaction
     * 
     * @param transaction
     *            The transaction to be declined
     * @return transaction
     * */
	@Override
	public Transaction declineTransaction(Transaction transaction) {
		logger.info("Declining transaction request");
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		transaction.setApprovalStatus("Declined");
		transaction.setModifiedOn(LocalDateTime.now());
		transaction.setModifiedBy(currentUser);
		transaction.setActive(false);
		transaction = transactionDao.update(transaction);
		return transaction;
	}
	
	/**
     * Approves new transfer transaction
     * 
     * @param transaction
     *            The transaction to be approved
     * @return transaction
     * */
	@Override
	public Transaction approveTransactionFromTransfer(Transaction transaction) {
		logger.info("Inside approve transaction");
		
		//check if transaction is pending
		if(transactionDao.findById(transaction.getTransactionId())==null){
			logger.info("Not a pending transaction");
			return null;
		}
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
	    
		Account account = transaction.getAccount();
		Double oldBalance = account.getBalance();
		Double newBalance = 0.0;
		if(transaction.getType().equalsIgnoreCase("DEBIT")){
			newBalance = oldBalance - transaction.getAmount();
		}
		else {
			newBalance = oldBalance + transaction.getAmount();
		}
		transaction.setApprovalStatus("Approved");
		transaction.setOldBalance(oldBalance);
		transaction.setNewBalance(newBalance);
		account.setBalance(newBalance);
		transaction.setModifiedOn(LocalDateTime.now());
		transaction.setModifiedBy(currentUser);
		transaction.setActive(true);
		transaction = transactionDao.save(transaction);
		account = accountDao.update(account);
		return transaction;
	}

	/**
     * Declines new transfer
     * 
     * @param transfer
     *            The transfer to be declined
     * @return transaction
     * */
	@Override
	public Transaction declineTransaction(Transfer transfer) {
		logger.info("Declining transfer request");
		//if transfer is not approved, no actions needed in transactions
		return null;
	}

	/**
     * Fetches pending transactions by account number
     * 
     * @param accountNumber
     *            The list of pending transactions
     * @return list of transactions
     * */
	@Override
	public List<Transaction> getPendingTransactionsByAccountNumber(Long accountNumber) {
		logger.info("Getting pending transactions by account number");
		if(accountNumber==null){
			return null;
		}
		List<Transaction> transactions = transactionDao.findPendingByAccount(accountNumber);
		return transactions;
	}
	
	/**
     * Fetches pending transactions by account number
     * 
     * @param accountNumber
     *            The list of pending transactions
     * @return list of transactions
     * */
	@Override
	public Transaction getPendingTransactionByAccountNumber(Long accountNumber) {
		logger.info("Getting pending transaction by account number");
		if(accountNumber==null){
			return null;
		}
		Transaction transactions = transactionDao.findPendingTransactionByAccount(accountNumber);
		return transactions;
	}

	/**
     * Fetches pending transactions by account number and account type
     * 
     * @param accountNumber
     * @param accountType
     *            The list of pending transactions
     * @return list of transactions
     * */
	@Override
	public List<Transaction> getPendingTransactionsByType(Long accountNumber, String accountType) {
		logger.info("Getting pending transactions by account number and account type");
		if(accountNumber==null||accountType==null){
			return null;
		}
		//List<Transaction> transactions = transactionDao.findPendingByAccountAndType(accountNumber, accountType);
		//return transactions;
		return null;
	}

	/**
     * Fetches pending transactions by approval status
     * 
     * @param approvalStatus
     *            The list of transactions 
     * @return list of transactions
     * */
	@Override
	public List<Transaction> getTransactionsByStatus(String approvalStatus) {
		logger.info("Getting pending transactions by approval status");
		if(approvalStatus==null){
			return null;
		}
		return transactionDao.findByApprovalStatus(approvalStatus);
	}

	/**
     * Fetches transaction by transactionId
     * 
     * @param transactionId
     *            The transactions 
     * @return transaction
     * */
	@Override
	public Transaction getTransactionById(UUID transactionId) {
		Transaction transaction = transactionDao.findById(transactionId);
		if (transaction == null || transaction.getActive() == false) {
			return null;
		}
		
		logger.info("Getting transaction by id");
		
		return transaction;
	}

	@Override
	public boolean isTransactionValid(Transaction transaction) {
		Account account = transaction.getAccount();
		Double pendingAmount = 0.0;
		
		//in Transaction model
		for(Transaction trans: transactionDao.findPendingByAccountAndType(account, "DEBIT")){
			pendingAmount += trans.getAmount();
		}	
		
		//check for pending transfer amounts
		for(Transfer transf: transferDao.findPendingTransferByFromAccount(account)){
			pendingAmount += transf.getAmount();
		}
		
		if(pendingAmount+transaction.getAmount() > transaction.getAccount().getBalance()){
			logger.info("Invalid transaction: amount requested is more than permitted");
			return false;
		}
		
		return true;
	}


}
