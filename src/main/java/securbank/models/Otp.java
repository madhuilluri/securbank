package securbank.models;

import java.util.Random;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.LocalDateTime;

/**
 * @author Mitikaa Sama
 *
 */

@Entity
@Table(name = "Otp")
public class Otp {
	
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@NotNull
	@Column(name = "OtpId", unique = true, nullable = false, columnDefinition = "BINARY(16)")
	private UUID OtpId;
	
	@NotNull
	@Column(name = "code", length = 6, nullable = false, updatable = false)
	private String code;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId", nullable = false)
	private User user;
	
	@NotNull
	@Column(name = "createdOn", nullable = false, updatable = false)
	private LocalDateTime createdOn;

	@NotNull
	@Column(name = "expireOn", nullable = false, updatable = false)
	private LocalDateTime expireOn;
	
	@NotNull
	@Column(name = "active", nullable = false, columnDefinition = "BIT")
	private Boolean active;
	
	private static final Random generator = new Random();
	
	public Otp(){
		
	}

	/**
	 * @param otpId
	 * @param code
	 * @param user
	 * @param createdOn
	 * @param expireOn
	 * @param active
	 */
	public Otp(UUID otpId, String code, User user, LocalDateTime createdOn, LocalDateTime expireOn, Boolean active) {
		OtpId = otpId;
		this.code = code;
		this.user = user;
		this.createdOn = createdOn;
		this.expireOn = expireOn;
		this.active = active;
	}

	/**
	 * @return the otpId
	 */
	public UUID getOtpId() {
		return OtpId;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the createdOn
	 */
	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	/**
	 * @return the expireOn
	 */
	public LocalDateTime getExpireOn() {
		return expireOn;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param otpId the otpId to set
	 */
	public void setOtpId(UUID otpId) {
		OtpId = otpId;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @param expireOn the expireOn to set
	 */
	public void setExpireOn(LocalDateTime expireOn) {
		this.expireOn = expireOn;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Otp [OtpId=" + OtpId + ", code=" + code + ", user=" + user + ", createdOn=" + createdOn + ", expireOn="
				+ expireOn + ", active=" + active + "]";
	}
	
	@PrePersist
	private void onCreate() {
		this.code = Integer.toString(generator.nextInt(900000) + 100000);
		this.active = true;
		this.createdOn = LocalDateTime.now();
		this.expireOn = LocalDateTime.now().plusMinutes(10);
	}
	
	
}