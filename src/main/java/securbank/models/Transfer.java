package securbank.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.LocalDateTime;

/**
 * @author Mitikaa Sama
 *
 * Sep 26, 2016
 */

@Entity
@Table(name = "Transfer")

public class Transfer {
	
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@NotNull
	@Column(name = "transferId", unique = true, nullable = false, columnDefinition = "BINARY(16)")
	private UUID transferId;
	
	/*@NotNull
	@Size(min = 8, max = 8)
	@Column(name = "fromAccountNumber", unique = false, nullable = false, updatable = false)
	private String fromAccountNumber;*/
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "fromAccount", nullable = false, updatable = false)
	private Account fromAccount;
	
	/*@NotNull
	@Size(min = 8, max = 8)
	@Column(name = "toAccountNumber", unique = false, nullable = false, updatable = false)
	private String toAccountNumber;*/
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "toAccount", nullable = false, updatable = false)
	private Account toAccount;
	
	@NotNull
	@Column(name = "amount", unique = false, nullable = false, updatable = false)
	private double amount;
	
	@NotNull
	@Column(name = "status", unique = false, nullable = false)
	private String status;
	
	@NotNull
	@Column(name = "createdOn", nullable = false, updatable = false)
	private LocalDateTime createdOn;
	
	@Column(name = "modifiedOn", nullable = true, updatable = true)
	private LocalDateTime modifiedOn;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "userId", nullable = true)
	private User modifiedBy;

	@NotNull
	@Column(name = "active", nullable = false, columnDefinition = "BIT")
	private Boolean active;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "transfer")
	private Set<Transaction> transactions = new HashSet<Transaction>(0);

	@NotNull
	@Column(name = "criticalStatus", unique = false, nullable = false, columnDefinition = "BIT")
	private boolean criticalStatus;
	
	@Transient
	@Column(name = "otp")
	private String otp;
	
	public Transfer(){
		
	}

	/**
	 * @param transferId
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 * @param status
	 * @param createdOn
	 * @param modifiedOn
	 * @param modifiedBy
	 * @param active
	 * @param transactions
	 * @param criticalStatus
	 * @param otp
	 */
	public Transfer(UUID transferId, Account fromAccount, Account toAccount, double amount, String status,
			LocalDateTime createdOn, LocalDateTime modifiedOn, User modifiedBy, Boolean active,
			Set<Transaction> transactions, boolean criticalStatus, String otp) {
		this.transferId = transferId;
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.amount = amount;
		this.status = status;
		this.createdOn = createdOn;
		this.modifiedOn = modifiedOn;
		this.modifiedBy = modifiedBy;
		this.active = active;
		this.transactions = transactions;
		this.criticalStatus = criticalStatus;
		this.otp = otp;
	}

	/**
	 * @return the transferId
	 */
	public UUID getTransferId() {
		return transferId;
	}

	/**
	 * @return the fromAccount
	 */
	public Account getFromAccount() {
		return fromAccount;
	}

	/**
	 * @return the toAccount
	 */
	public Account getToAccount() {
		return toAccount;
	}

	/**
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the createdOn
	 */
	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	/**
	 * @return the modifiedOn
	 */
	public LocalDateTime getModifiedOn() {
		return modifiedOn;
	}

	/**
	 * @return the modifiedBy
	 */
	public User getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @return the transactions
	 */
	public Set<Transaction> getTransactions() {
		return transactions;
	}

	/**
	 * @return the criticalStatus
	 */
	public boolean isCriticalStatus() {
		return criticalStatus;
	}

	/**
	 * @return the otp
	 */
	public String getOtp() {
		return otp;
	}

	/**
	 * @param transferId the transferId to set
	 */
	public void setTransferId(UUID transferId) {
		this.transferId = transferId;
	}

	/**
	 * @param fromAccount the fromAccount to set
	 */
	public void setFromAccount(Account fromAccount) {
		this.fromAccount = fromAccount;
	}

	/**
	 * @param toAccount the toAccount to set
	 */
	public void setToAccount(Account toAccount) {
		this.toAccount = toAccount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @param modifiedOn the modifiedOn to set
	 */
	public void setModifiedOn(LocalDateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * @param transactions the transactions to set
	 */
	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
	}

	/**
	 * @param criticalStatus the criticalStatus to set
	 */
	public void setCriticalStatus(boolean criticalStatus) {
		this.criticalStatus = criticalStatus;
	}

	/**
	 * @param otp the otp to set
	 */
	public void setOtp(String otp) {
		this.otp = otp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Transfer [transferId=" + transferId + ", fromAccount=" + fromAccount + ", toAccount=" + toAccount
				+ ", amount=" + amount + ", status=" + status + ", createdOn=" + createdOn + ", modifiedOn="
				+ modifiedOn + ", modifiedBy=" + modifiedBy + ", active=" + active + ", transactions=" + transactions
				+ ", criticalStatus=" + criticalStatus + ", otp=" + otp + "]";
	}

	
}
	
	