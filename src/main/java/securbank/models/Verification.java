package securbank.models;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.LocalDateTime;

/**
 * 
 * @author Ayush Gupta
 */

@Entity 
@Table(name = "Verification")
public class Verification {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@NotNull
	@Column(name = "verificationId", unique = true, nullable = false, columnDefinition = "BINARY(16)")
	private UUID verificationId;

	/** multiple account can be associated with an user	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId", nullable = false)
	private User user;
	
	@NotNull
	@Column(name = "createdOn", updatable = false)
	private LocalDateTime createdOn;
	
	@NotNull
	private LocalDateTime expireOn;
	
	@NotNull
	private String type;
	
	/**
	 * 
	 */
	public Verification() {

	}

	/**
	 * @param verificationId
	 * @param user
	 * @param createdOn
	 * @param expireOn
	 * @param type
	 */
	public Verification(UUID verificationId, User user, LocalDateTime createdOn, LocalDateTime expireOn, String type) {
		super();
		this.verificationId = verificationId;
		this.user = user;
		this.createdOn = createdOn;
		this.expireOn = expireOn;
		this.type = type;
	}

	/**
	 * @return the verificationId
	 */
	public UUID getVerificationId() {
		return verificationId;
	}

	/**
	 * @param verificationId the verificationId to set
	 */
	public void setVerificationId(UUID verificationId) {
		this.verificationId = verificationId;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the createdOn
	 */
	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the expireOn
	 */
	public LocalDateTime getExpireOn() {
		return expireOn;
	}

	/**
	 * @param expireOn the expireOn to set
	 */
	public void setExpireOn(LocalDateTime expireOn) {
		this.expireOn = expireOn;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Verification [verificationId=" + verificationId + ", user=" + user + ", createdOn=" + createdOn
				+ ", expireOn=" + expireOn + ", type=" + type + "]";
	}
}
