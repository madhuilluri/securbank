package securbank.dao;

import java.util.List;
import java.util.UUID;

import securbank.models.Account;
import securbank.models.Transfer;
import securbank.models.User;

/**
 * @author Mitikaa Sama
 *
 * Sep 26, 2016
 */
public interface TransferDao extends BaseDao<Transfer, UUID>{
	public List<Transfer> findAll();
	public List<Transfer> findTransferByFromAccount(Account fromAccount);
	public List<Transfer> findTransferByToAccount(Account toAccount);
	public List<Transfer> findByApprovalStatus(String approvalStatus);
	public List<Transfer> findPendingTransferByFromAccount(Account fromAccount);
	public List<Transfer> findByUserAndApprovalStatus(User user, String status);
}
