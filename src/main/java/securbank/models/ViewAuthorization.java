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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

/**
 * @author Ayush Gupta
 *
 */
@Entity
@Table(name = "ViewAuthorization")
public class ViewAuthorization {
	
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "id", unique = true, columnDefinition = "BINARY(16)")
	private UUID id;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "external", referencedColumnName = "userId", nullable = false)
	private User external;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee", referencedColumnName = "userId", nullable = false)
	private User employee;

	@NotNull
	@Column(name = "active", columnDefinition = "BIT")
	private Boolean active;
	
	@Transient
	private String status;
	
	public ViewAuthorization() {
		
	}

	/**
	 * @param id
	 * @param external
	 * @param employee
	 * @param active
	 * @param status
	 */
	public ViewAuthorization(UUID id, User external, User employee, Boolean active, String status) {
		super();
		this.id = id;
		this.external = external;
		this.employee = employee;
		this.active = active;
		this.status = status;
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @return the external
	 */
	public User getExternal() {
		return external;
	}

	/**
	 * @param external the external to set
	 */
	public void setExternal(User external) {
		this.external = external;
	}

	/**
	 * @return the employee
	 */
	public User getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(User employee) {
		this.employee = employee;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ViewAuthorization [id=" + id + ", external=" + external + ", employee=" + employee + ", active="
				+ active + ", status=" + status + "]";
	}
}
