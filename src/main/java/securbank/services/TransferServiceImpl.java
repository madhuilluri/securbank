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


@Service("transferService")
@Transactional
public class TransferServiceImpl implements TransferService{

	@Autowired
	private TransferDao transferDao;
	
	@Autowired
	private TransactionDao transactionDao;
	
	@Autowired
	private AccountDao accountDao;
	
	@Autowired 
	private AccountService accountService;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	OtpService otpService;
	
	@Autowired
	private Environment env;
	
	private SimpleMailMessage message;
	
	final static Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);
	
	/**
     * Initiates new transfer
     * 
     * @param transfer
     *            The transfer to be initiated
     * @return transfer
     */
	@Override
	public Transfer initiateTransfer(Transfer transfer) {
		logger.info("Initiating new transfer request");
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		
		//accountTo and accountFrom should not have same email address
		if(currentUser.getEmail().equalsIgnoreCase(transfer.getToAccount().getUser().getEmail())){
			logger.info("Transfer to and from accounts are same");
			return null;
		}
		
		//get user's checking account
		logger.info("Getting current user's checking account");
		for (Account acc: currentUser.getAccounts()){
			if (acc.getType().equalsIgnoreCase("checking")){
				transfer.setFromAccount(acc);
			}
		}
		
		//check otp
		if(!transfer.getOtp().equals(otpService.getOtpByUser(transfer.getFromAccount().getUser()).getCode())){
			logger.info("Otp mismatch");
			return null;
		}
		
		if(isTransferValid(transfer)==false){
			return null;
		}
		
		//check if account exists and is active
		if(isToAccountValid(transfer)==false){
			return null;
		}
		
		//get user's checking account
		logger.info("Getting ToAccount user's checking account");
		User toUser = userService.getUserByEmail(transfer.getToAccount().getUser().getEmail());
		for (Account acc: toUser.getAccounts()){
			if (acc.getType().equalsIgnoreCase("checking")){
				transfer.setToAccount(acc);
			}
		}
		
		//user types should be external
		if(!"ROLE_INDIVIDUAL".equalsIgnoreCase(currentUser.getRole()) && !"ROLE_MERCHANT".equalsIgnoreCase(currentUser.getEmail())){
			logger.info("Current user is not an external user");
			return null;
		}
		
		if(!"ROLE_INDIVIDUAL".equalsIgnoreCase(transfer.getToAccount().getUser().getRole()) && !"ROLE_MERCHANT".equalsIgnoreCase(transfer.getToAccount().getUser().getRole())){
			logger.info("To account user is not an external user");
			return null;
		}

		transfer.setStatus("Pending");
		transfer.setActive(true);
		transfer.setCreatedOn(LocalDateTime.now());
		
		transferDao.save(transfer);
		logger.info("After transferDao save");
		
		//send email to sender
		User user = transfer.getFromAccount().getUser();
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.transfer.to.body"));
		message.setSubject(env.getProperty("external.user.transfer.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		//send email to sender
		user = transfer.getToAccount().getUser();
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.transfer.from.body"));
		message.setSubject(env.getProperty("external.user.transfer.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		return transfer;
	}

	/**
     * Approves new transfer
     * 
     * @param transfer
     *            The transfer to be initiated
     * @return transfer
     */
	@Override
	public Transfer approveTransfer(Transfer transfer) {
		logger.info("Inside approve transfer");
		
		//check if transaction is pending
		if(transferDao.findById(transfer.getTransferId())==null){
			logger.info("Not a pending transfer");
			return null;
		}
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		
		transfer.setStatus("Approved");
		transfer.setModifiedBy(currentUser);
		
		//call transaction Service to create new approved debit and credit transactions
		transactionService.approveTransfer(transfer);
		
		return transfer;
	}

	/**
     * Declines new transfer
     * 
     * @param transfer
     *            The transfer to be initiated
     * @return transfer
     */
	@Override
	public Transfer declineTransfer(Transfer transfer) {
		logger.info("Inside decline transfer");
		
		//check if transaction is pending
		if(transferDao.findById(transfer.getTransferId())==null){
			logger.info("Not a pending transfer");
			return null;
		}
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		
		transfer.setStatus("Declined");
		
		return transfer;
	}

	/**
     * Fetches all pending transfers by account number
     * 
     * @param accountNumber
     *            The list of pending transfer
     * @return list of transfer
     * */
	@Override
	public List<Transfer> getPendingTransfersByAccountNumber(Long accountNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * Fetches all pending transfers by account number and account type
     * 
     * @param accountNumber
     * @param accountType
     *            The list of pending transfer
     * @return list of transfer
     * */
	@Override
	public List<Transfer> getPendingTransfersByType(Long accountNumber, String accountType) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * Fetches all pending transfers by approval status
     * 
     * @param approvalStatus
     *            The list of pending transfers
     * @return list of transfer
     * */
	@Override
	public List<Transfer> getTransfersByStatus(String approvalStatus) {
		logger.info("Getting pending transfers by approval status");
		if(approvalStatus==null){
			return null;
		}
		return transferDao.findByApprovalStatus(approvalStatus);
	}
	
	/**
     * Fetches all pending transfers by approval status
     * 
     * @param approvalStatus
     *            The list of pending transfers
     * @return list of transfer
     * */
	@Override
	public List<Transfer> getTransfersByStatusAndUser(User user, String approvalStatus) {
		logger.info("Get waiting transfers by approval status");
		if(approvalStatus==null){
			return null;
		}
		return transferDao.findByUserAndApprovalStatus(user, approvalStatus);
	}
	
	
	/**
     * Fetches transfer by transactionId
     * 
     * @param transactionId
     *           pending transfer
     * @return transfer
     * */
	@Override
	public Transfer getTransferById(UUID id) {
		logger.info("Getting pending transfers by transfer id");
		Transfer transfer = transferDao.findById(id);
		if (transfer == null || transfer.getActive() == false) {
			return null;
		}
		return transferDao.findById(id);
	}

	/**
     * Fetches transfer by accountNumber
     * 
     * @param accountNumber
     *           pending transfer
     * @return transfer
     * */
	@Override
	public Transfer getPendingTransfernByAccountNumber(Long accountNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTransferValid(Transfer transfer) {
		Account account = transfer.getFromAccount();
		Double pendingAmount = 0.0;
		
		//check for pending transfer amounts
		for(Transfer transf: transferDao.findPendingTransferByFromAccount(account)){
			pendingAmount += transf.getAmount();
		}	
				
		//check for pending transaction amounts
		for(Transaction trans: transactionDao.findPendingByAccountAndType(account, "DEBIT")){
			pendingAmount += trans.getAmount();
		}
				
		//check if transfer amount is valid
		if(pendingAmount+transfer.getAmount() > transfer.getFromAccount().getBalance()){
			logger.info("Invalid transfer: amount requested to be debitted is more than permitted");
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isToAccountValid(Transfer transfer) {
		if(userService.getUserByEmail(transfer.getToAccount().getUser().getEmail())!=null){
			return true;
		}
		return false;
	}

	@Override
	public Transfer initiateMerchantPaymentRequest(Transfer transfer) {
		logger.info("Initiating new payment request");
		
		User currentUser = userService.getCurrentUser();
		if(currentUser==null){
			logger.info("Current logged in user is null");
			return null;
		}
		
		//accountTo and accountFrom should not have same email address
		if(currentUser.getEmail().equalsIgnoreCase(transfer.getFromAccount().getUser().getEmail())){
			logger.info("Transfer to and from accounts are same");
			return null;
		}
		
		//get user's checking account
		logger.info("Getting current user's checking account");
		for (Account acc: currentUser.getAccounts()){
			if (acc.getType().equalsIgnoreCase("checking")){
				transfer.setToAccount(acc);
			}
		}
		
		//check otp
		if(!transfer.getOtp().equals(otpService.getOtpByUser(transfer.getToAccount().getUser()).getCode())){
			logger.info("Otp mismatch");
			return null;
		}
		
		//get user's checking account
		logger.info("Getting fromAccount user's checking account");
		User fromUser = userService.getUserByEmail(transfer.getFromAccount().getUser().getEmail());
		for (Account acc: fromUser.getAccounts()){
			if (acc.getType().equalsIgnoreCase("checking")){
				transfer.setFromAccount(acc);
			}
		}
		
		if(isTransferValid(transfer)==false){
			return null;
		}
		
		//check if account exists and is active
		if(isToAccountValid(transfer)==false){
			return null;
		}
		
		//user types should be external
		if(!"ROLE_MERCHANT".equalsIgnoreCase(currentUser.getRole())){
			logger.info("Current user is not a merchant");
			return null;
		}
		
		if(!"ROLE_INDIVIDUAL".equalsIgnoreCase(transfer.getFromAccount().getUser().getRole())){
			logger.info("From account user is not an external user");
			return null;
		}

		transfer.setStatus("Waiting");
		transfer.setActive(true);
		transfer.setCreatedOn(LocalDateTime.now());
		
		transferDao.save(transfer);
		logger.info("After transferDao save");
		
		//send email to sender
		User user = transfer.getFromAccount().getUser();
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.transfer.to.body"));
		message.setSubject(env.getProperty("external.user.transfer.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		//send email to sender
		user = transfer.getToAccount().getUser();
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.transfer.from.body"));
		message.setSubject(env.getProperty("external.user.transfer.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		return transfer;
	}

	@Override
	public Transfer approveTransferToPending(Transfer transfer) {
		transfer.setStatus("Pending");
		transfer.setModifiedBy(userService.getCurrentUser());
		transfer.setModifiedOn(LocalDateTime.now());
		transferDao.update(transfer);
		
		return transfer;
	}

}
