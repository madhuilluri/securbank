package securbank.models;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "pii")
public class Pii {
	
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "pId", unique = true, columnDefinition = "BINARY(16)")
	private UUID pId;
	
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "userId",referencedColumnName = "userId", nullable = false)
	private User user;
	
	@NotNull
	@Size(min =9, max = 9)
	private String ssn;
	
	public Pii(){
		
	}

	/**
	 * @param pId
	 * @param user
	 * @param ssn
	 */
	public Pii(UUID pId, User user, String ssn) {
		super();
		this.pId = pId;
		this.user = user;
		this.ssn = ssn;
	}

	/**
	 * @return the pId
	 */
	public UUID getpId() {
		return pId;
	}

	/**
	 * @param pId the pId to set
	 */
	public void setpId(UUID pId) {
		this.pId = pId;
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
	 * @return the ssn
	 */
	public String getSsn() {
		return ssn;
	}

	/**
	 * @param ssn the ssn to set
	 */
	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pii [pId=" + pId + ", user=" + user + ", ssn=" + ssn + "]";
	}

}
