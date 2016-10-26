package securbank.models;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.LocalDateTime;
//import org.springframework.data.annotation.Id;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author Sagar Navgire
 *
 */

@Entity
@Table(name = "LoginAttempt")
public class LoginAttempt {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "loginattemptId", unique = true, columnDefinition = "BINARY(16)")
	private UUID loginattemptId;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "userId", nullable = false)
	private User user;
	
	@Column(name = "counter")
	private Integer counter;
	
	@NotNull
	@Column(name = "LastUpdated", nullable = false)
	private LocalDateTime LastUpdated;
	
	
	public LoginAttempt(User user, Integer counter, LocalDateTime LastUpdated){
		super();
		this.user = user;
		this.counter = counter;
		this.LastUpdated = LastUpdated;
	}
	
	public LoginAttempt(){
		
	}	
	
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param account the account to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return the counter
	 */
	public Integer getCounter() {
		return counter;
	}

	/**
	 * @param Increases counter by one
	 */
	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	
	/**
	 * @return the createdOn Date
	 */
	public LocalDateTime getLastUpdated() {
		return LastUpdated;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setLastUpdated(LocalDateTime LastUpdated) {
		this.LastUpdated = LastUpdated;
	}
	
	@Override
	public String toString() {
		return "LoginAttempt [ user=" + user +  ", counter=" + counter
				+ ", Last Updated=" + LastUpdated + "]";
	}
}
