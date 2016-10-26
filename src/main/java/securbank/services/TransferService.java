package securbank.services;

import java.util.List;
import java.util.UUID;

import securbank.models.Transfer;
import securbank.models.User;

/**
 * @author Mitikaa
 *
 */
public interface TransferService {
	public Transfer initiateTransfer(Transfer transfer);
	public Transfer approveTransfer(Transfer transfer);
	public Transfer declineTransfer(Transfer transfer);
	public List<Transfer> getPendingTransfersByAccountNumber(Long accountNumber);
	public List<Transfer> getPendingTransfersByType(Long accountNumber, String accountType);
	public List<Transfer> getTransfersByStatus(String approvalStatus);
	public List<Transfer> getTransfersByStatusAndUser(User user, String approvalStatus);
	public Transfer getTransferById(UUID id);
	public Transfer getPendingTransfernByAccountNumber(Long accountNumber);
	public boolean isTransferValid(Transfer transfer);
	public boolean isToAccountValid(Transfer transfer);
	public Transfer initiateMerchantPaymentRequest(Transfer transfer);
	public Transfer approveTransferToPending(Transfer transfer);
}
