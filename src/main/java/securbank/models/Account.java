package securbank.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDateTime;

/**
 * 
 * @author Madhu Illuri
 */

@Entity 
@Table(name = "Account")
public class Account {
	
	/** Account number is unique. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountNumber;
	
	/** multiple account can be associated with an user	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "userId", nullable = false)
	private User user;
	
	/** balance on the account */
	@NotNull
	private Double balance;
	
	@NotNull
	private String type;
	
	@NotNull
	@Column(name = "createdOn", updatable = false)
	private LocalDateTime createdOn;
	
	@NotNull
	@Column(name = "active", columnDefinition = "BIT")
	private Boolean active;
	
	/**
	 * Changes related to transactions made my Mitikaa on 10/10
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
	private Set<Transaction> transactions = new HashSet<Transaction>(0);
	
	/**
	 * 
	 * @param accountNumber
	 * @param user
	 * @param balance
	 * @param accountType
	 * @param transactions
	 * @param createdOn
	 * @param active
	 */
	public Account(Long accountNumber, User user, double balance, String type, Set<Transaction> transactions, 
			LocalDateTime createdOn, Boolean active){
		super();
		this.accountNumber = accountNumber;
		this.user = user;
		this.balance = balance;
		this.type = type;
		this.transactions = transactions;
		this.createdOn = createdOn;
		this.active = active;	
		
	}
	
	public Account() {
		
	}
	
	/**
	 * 
	 * @return accountNumber
	 */
	public Long getAccountNumber() {
		return accountNumber;
	}
	
	/**
	 * 
	 * @param accountNumber sets accountNumber
	 */
	public void setAccountNumber(Long accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	/**
	 * 
	 * @return user
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * 
	 * @param user sets user
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * 
	 * @return balance
	 */
	public Double getBalance() {
		return balance;
	}
	
	/**
	 * 
	 * @param balance sets balance
	 */
	public void setBalance(Double balance) {
		this.balance = balance;
	}

	/**
	 * 
	 * @return type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * 
	 * @param type sets type
	 */
	public void setType(String type) {
		this.type = type;
	}
		
	
	/**
	 * added setters and getter for transactions 
	 * -Mitikaa
	 */
	
	/**
	 * @return the transactions
	 */
	public Set<Transaction> getTransactions() {
		return transactions;
	}

	/**
	 * @param transactions the transactions to set
	 */
	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
	}

	/**
	 * 
	 * @return createdOn time created on
	 */
	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	/**
	 * 
	 * @param createdOn sets time created on 
	 */
	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}
		
	/**
	 * 
	 * @return active if the account is active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * 
	 * @param active sets account active
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Account [accountNumber=" + accountNumber + ", user=" + user + ", balance=" + balance + ", type=" + type
				+ ", createdOn=" + createdOn + ", active=" + active + ", transactions=" + transactions + "]";
	}
	
	
}
