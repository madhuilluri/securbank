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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import securbank.dao.ModificationRequestDao;
import securbank.dao.NewUserRequestDao;
import securbank.dao.UserDao;
import securbank.models.Account;
import securbank.models.ChangePasswordRequest;
import securbank.models.ModificationRequest;
import securbank.models.NewUserRequest;
import securbank.models.Pii;
import securbank.models.User;
import securbank.models.LoginAttempt;

/**
 * @author Ayush Gupta
 *
 */
@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private NewUserRequestDao newUserRequestDao;
	
	@Autowired
	private ModificationRequestDao modificationRequestDao;
	
	@Autowired 
	private AccountService accountService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PasswordEncoder encoder;
	
	private SimpleMailMessage message;
	
	@Autowired
	private Environment env;
	
	final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	/**
     * Creates new user
     * 
     * @param user
     *            The user to be created
     * @return user
     */
	@Override
	public User createExternalUser(User user) {
		Pii pii = new Pii();
		
		logger.info("Creating new external user");
		logger.info(user.toString());
		user.setPassword(encoder.encode(user.getPassword()));
		user.setCreatedOn(LocalDateTime.now());
		user.setActive(false);
		user.setType("external");
		
		LoginAttempt attempt = new LoginAttempt(user, 0, LocalDateTime.now());		
		user.setLoginAttempt(attempt);
		
		pii.setUser(user);
		pii.setSsn(user.getPii().getSsn());
		user.setPii(pii);
		user = userDao.save(user);
		
		
		//setup up email message
		message = new SimpleMailMessage();
		message.setText(env.getProperty("external.user.verification.body").replace(":id:",user.getUserId().toString()));
		message.setSubject(env.getProperty("external.user.verification.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		return user;
	}
	
	/**
     * Creates new user
     * 
     * @param user
     *            The user to be created
     * @return user
     */
	@Override
	public User createInternalUser(User user) {
		NewUserRequest newUserRequest = new NewUserRequest();
		Pii pii = new Pii();
		// verify if request exists
		newUserRequest = newUserRequestDao.findByEmailAndRole(user.getEmail(), user.getRole()); 
		if (newUserRequest == null) {
			logger.info("Invalid request for new internal user");
			return null;
		}

		LoginAttempt attempt = new LoginAttempt();
		attempt.setLastUpdated(LocalDateTime.now());
		attempt.setCounter(0);
		attempt.setUser(user);
		user.setLoginAttempt(attempt);
		
		// Deactivates request
		newUserRequest.setActive(false);
		newUserRequestDao.update(newUserRequest);
		
		logger.info("Creating new internal user");
		
		// creates new user
		user.setType("internal");
		user.setPassword(encoder.encode(user.getPassword()));
		user.setCreatedOn(LocalDateTime.now());
		user.setActive(true);
		
		pii.setUser(user);
		pii.setSsn(user.getPii().getSsn());
		user.setPii(pii);
		return userDao.save(user);
	}
	
	/**
     * Verify new user
     * @param userId
     * 			User id to be verified
     * @return user
     */
	@Override
	public boolean verifyNewUser(UUID userId) {
		User user = userDao.findById(userId);
		if (user == null || userDao.emailExists(user.getEmail()) || userDao.phoneExists(user.getPhone()) || userDao.usernameExists(user.getUsername())) {
			logger.info("Verification for existing email, phone or username");
			return false;
		}
		
		if (user.getActive() == true) {
			logger.info("Verification for active user");
			return true;
		}
		
		logger.info("Verifying and create account for new external user");
		
		user.setActive(true);
		user = userDao.update(user);

		// Creates new checking account
		Account account = new Account();
		account.setUser(user);
		account.setType("checking");
		account.setBalance(0.0);
		account = accountService.createAccount(account);
				
		if (user == null) {
			return false;
		}
		
		return true;
	}
	
	/**
     * Get current logged in user
     * 
     * @return user
     */
	@Override
	public User getCurrentUser() {
		String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (username == null) {
			return null;
		}
		
		logger.info("Getting current logged in user");
		return userDao.findByUsernameOrEmail(username);
	}

	/**
     * Edit user
     * @param user
     * 			User to be edited
     * @return user
     */
	public User editUser(User user) {
		User current = userDao.findById(user.getUserId());
		current.setEmail(user.getEmail());
		current.setPhone(user.getPhone());
		current.setFirstName(user.getFirstName());
		current.setMiddleName(user.getMiddleName());
		current.setLastName(user.getLastName());
		current.setAddressLine1(user.getAddressLine1());
		current.setAddressLine2(user.getAddressLine2());
		current.setCity(user.getCity());
		current.setState(user.getState());
		current.setZip(user.getZip());
		current.setModifiedOn(LocalDateTime.now());
		current.setRole(user.getRole());
		current = userDao.update(current);
		
		return current;
	}
	
	/**
     * Delete user
     * @param user
     * 			User to be deleted
     * @return void
     */
	public void deleteUser(UUID id) {
		User current = userDao.findById(id);
		userDao.remove(current);
		
		return;
	}
	
	/**
     * Get all users by type
     *
	 * @return List<User>
     */
	@Override
	public List<User> getUsersByType(String type) {
		List<User> users = userDao.findAllByType(type);
		logger.info("Getting users by type");
		
		return users;
	}
	
	/**
     * Get all users by id
     *
	 * @return user
     */
	@Override
	public User getUserByIdAndActive(UUID id) {
		User user = userDao.findById(id);
		if (user == null || user.getActive() == false) {
			return null;
		}
		
		logger.info("Getting user by id");
		
		return user;
	}
	
	/**
     * Creates new user request
     * 
     * @param newUserRequest
     *            The user request to be created
     * @return newUserRequest
     */
	@Override
	public NewUserRequest createNewUserRequest(NewUserRequest newUserRequest) {
		newUserRequest.setCreatedOn(LocalDateTime.now());
		newUserRequest.setExpireOn(LocalDateTime.now().plusDays(1));
		newUserRequest.setActive(true);
		newUserRequest = newUserRequestDao.save(newUserRequest);
		
		logger.info("Creating new internal user request");
		
		//setup up email message
		message = new SimpleMailMessage();
		message.setText(env.getProperty("internal.user.verification.body").replace(":id:",newUserRequest.getNewUserRequestId().toString()));
		message.setSubject(env.getProperty("internal.user.verification.subject"));
		message.setTo(newUserRequest.getEmail());
		
		// send email
		if (emailService.sendEmail(message) == false) {
			// Deactivate request if email fails
			newUserRequest.setActive(false);
			newUserRequestDao.update(newUserRequest);
			logger.warn("Email message failed");
			
			return null;
		};
		
		return newUserRequest;
	}
	
	/**
     * Get new user request and deactivates it
     * @param newUserRequestId
     *            The user request id to be retrieved
     * @return newUserRequest
     */
	@Override
	public NewUserRequest getNewUserRequest(UUID newUserRequestId) {
		NewUserRequest newUserRequest = newUserRequestDao.findById(newUserRequestId);
		if (newUserRequest == null) {
			logger.info("Request for getting new user request with invalid id");
			return null;
		}
		logger.info("Getting new user request by id");
		return newUserRequest;
	}

	/**
     * Creates external user modification request
     * 
     * @param request
     *            The modification request to be create 
     * @return modificationRequest
     */
	@Override
	public ModificationRequest createInternalModificationRequest(User user) {
		User current = getCurrentUser();
		ModificationRequest request = new ModificationRequest();
		if (user == null) {
			logger.warn("Request for user who is not logged in");
			
			return null;
		}
		List<ModificationRequest> requests = modificationRequestDao.findAllbyUser(current);
		logger.debug("Deactivating existing modification requests");
		
		// Deactivate all active request 
		for (ModificationRequest activeRequest : requests) {
			activeRequest.setActive(false);
			modificationRequestDao.update(activeRequest);
		}
		
		if (!user.getEmail().equals(current.getEmail())) {
			request.setStatus("waiting");
		}
		else {
			request.setStatus("pending");
		}
		request.setUsername(current.getUsername());
		request.setEmail(user.getEmail());
		request.setPhone(user.getPhone());
		request.setFirstName(user.getFirstName());
		request.setMiddleName(user.getMiddleName());
		request.setLastName(user.getLastName());
		request.setAddressLine1(user.getAddressLine1());
		request.setAddressLine2(user.getAddressLine2());
		request.setCity(user.getCity());
		request.setState(user.getState());
		request.setZip(user.getZip());
		request.setUser(current);
		request.setPassword(current.getPassword());
		request.setActive(true);
		request.setCreatedOn(LocalDateTime.now());
		request.setUserType("internal");
		request.setRole(user.getRole());
		request = modificationRequestDao.save(request);
		logger.info("Request for creating new internal user modification request");
		
		if (!request.getEmail().equals(current.getEmail())) {
			message = new SimpleMailMessage(); 
			message.setText(env.getProperty("modification.request.verify.body").replace(":id:", request.getModificationRequestId().toString()));
			message.setSubject(env.getProperty("modification.request.verify.subject"));
			message.setTo(request.getEmail());
			emailService.sendEmail(message);
		}
		
		return request;
	}

	/**
     * Creates external user modification request
     * 
     * @param user
     *            The modification request to be create 
     * @return modificationRequest
     */
	@Override
	public ModificationRequest createExternalModificationRequest(User user) {
		User current = getCurrentUser();
		if (user == null) {
			return null;
		}
		ModificationRequest request = new ModificationRequest();
		List<ModificationRequest> requests = modificationRequestDao.findAllbyUser(current);
		
		// Deactivate all active request 
		for (ModificationRequest activeRequest : requests) {
			activeRequest.setActive(false);
			modificationRequestDao.update(activeRequest);
		}
		
		if (!user.getEmail().equals(current.getEmail())) {
			request.setStatus("waiting");
		}
		else {
			request.setStatus("pending");
		}
		request.setUsername(current.getUsername());
		request.setEmail(user.getEmail());
		request.setPhone(user.getPhone());
		request.setFirstName(user.getFirstName());
		request.setMiddleName(user.getMiddleName());
		request.setLastName(user.getLastName());
		request.setAddressLine1(user.getAddressLine1());
		request.setAddressLine2(user.getAddressLine2());
		request.setCity(user.getCity());
		request.setState(user.getState());
		request.setZip(user.getZip());
		request.setUser(current);
		request.setPassword(current.getPassword());
		request.setActive(true);
		request.setCreatedOn(LocalDateTime.now());
		request.setUserType("external");
		request.setRole(current.getRole());
		request = modificationRequestDao.save(request);
		logger.info("Request for creating new external user modification request");
		
		if (!request.getEmail().equals(current.getEmail())) {
			message = new SimpleMailMessage();
			message.setText(env.getProperty("modification.request.verify.body").replace(":id:", request.getModificationRequestId().toString()));
			message.setSubject(env.getProperty("modification.request.verify.subject"));
			message.setTo(request.getEmail());
			emailService.sendEmail(message);
		}
				
		return request;
	}
	
	/**
     * Approves user request
     * 
     * @param requestId
     *            The id of the request to be approved 
     * @return modificationRequest
     */
	@Override
	public ModificationRequest approveModificationRequest(ModificationRequest request) {
		ModificationRequest current = modificationRequestDao.findById(request.getModificationRequestId());
		User user = current.getUser();
		
		// If email has been taken
		if ((!request.getEmail().equals(user.getEmail()) && (userDao.emailExists(request.getEmail()) || newUserRequestDao.emailExists(request.getEmail())))
				|| (!request.getPhone().equals(user.getPhone()) && userDao.phoneExists(request.getPhone()))) {
			logger.info("Rejecting request due to unique contraint conflict");
			
			// Sends an email if email and phone clash with existing users
			SimpleMailMessage message = new SimpleMailMessage();
			message.setText(env.getProperty("modification.request.failure.body"));
			message.setSubject(env.getProperty("modification.request.failure.subject"));
			message.setTo(user.getEmail());
			emailService.sendEmail(message);
			
			// update request
			request.setActive(false);
			request.setStatus("rejected");
			request.setModifiedOn(LocalDateTime.now());
			modificationRequestDao.update(request);
			
			return null;
		}
		logger.info("Request for approving modification request");
		
		current.setEmail(request.getEmail());
		current.setPhone(request.getPhone());
		current.setFirstName(request.getFirstName());
		current.setMiddleName(request.getMiddleName());
		current.setLastName(request.getLastName());
		current.setAddressLine1(request.getAddressLine1());
		current.setAddressLine2(request.getAddressLine2());
		current.setCity(request.getCity());
		current.setState(request.getState());
		current.setZip(request.getZip());
		
		// Update User
		user.setFirstName(request.getFirstName());
		user.setMiddleName(request.getMiddleName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setAddressLine1(request.getAddressLine1());
		user.setAddressLine2(request.getAddressLine2());
		user.setCity(request.getCity());
		user.setState(request.getState());
		user.setZip(request.getZip());
		if (request.getUserType().equals("internal")) {
			user.setRole(request.getRole());
		}
		user.setModifiedOn(LocalDateTime.now());
		userDao.update(user);
		
		// Update request
		current.setActive(false);
		current.setModifiedOn(LocalDateTime.now());
		current.setApprovedBy(getCurrentUser());
		current.setStatus("approved");
		current = modificationRequestDao.update(current);
		
		return current;
	}
	
	/**
     * Rejects user request
     * 
     * @param requestId
     *            The id of the request to be approved 
     * @return modificationRequest
     */
	@Override
	public ModificationRequest rejectModificationRequest(ModificationRequest request) {
		ModificationRequest current = modificationRequestDao.findById(request.getModificationRequestId());
		User user = current.getUser();
			
		logger.info("Request for rejecting modification request");
		
		// update request
		current.setActive(false);
		current.setStatus("rejected");
		current.setModifiedOn(LocalDateTime.now());
		current.setApprovedBy(getCurrentUser());
		current = modificationRequestDao.update(current);
		// Sends an email if request is rejected
		SimpleMailMessage message = new SimpleMailMessage();
		message.setText(env.getProperty("modification.request.reject.body"));
		message.setSubject(env.getProperty("modification.request.reject.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);
		
		return current;
	}
	
	/**
     * Get all pending internal user request
     * 
     * @param type
     *            The type of user 
     * @param status
     *            The status of request 
     * @return List<modificationRequest>
     */
	public List<ModificationRequest> getModificationRequests(String status, String type) {
		logger.info("Getting all modification request by user type and status of request");
		
		return modificationRequestDao.findAllbyStatusAndUserType(status, type);
	}
	
	/**
     * Get request by Id
     * 
     * @param requestId
     *            The id of the request to be retrieved 
     * @return modificationRequest
     */
	public ModificationRequest getModificationRequest(UUID requestId) {
		logger.info("Getting modification request by ID");
		
		return modificationRequestDao.findById(requestId);
	}
	
	/**
     * Verify email and update request to pending
     * 
     * @param status
     *            The status of the request 
     * @param requestId
     *            The id of the request to be verified 
     * @return boolean
     */
	public boolean verifyModificationRequest(String status, UUID requestId) {
		ModificationRequest request = modificationRequestDao.findById(requestId);
		if (request == null) {
			return false;
		}
		if (!request.getStatus().equals(status)) {
			return false;
		}
	
		logger.info("Verifying changes email address of user");
		
		request.setStatus("pending");
		modificationRequestDao.update(request);
		
		return true;
	}

	/**
     * Verify the usertype of request
     * 
     * @param requestId
     *            The id of the request to be verified 
     * @param type
     *            The type of user of the request 
     * @return boolean
     */
	public boolean verifyModificationRequestUserType(UUID requestId, String type) {
		ModificationRequest request = modificationRequestDao.findById(requestId);
		if (request == null) {
			return false;
		}
		if (!request.getUserType().equals(type)) {
			return false;
		}
	
		logger.info("Verifying type of user for request");
		
		return true;
	}

	/**
     * Deletes a request
     * 
     * @param request
     *            The request to be deleted 
     */
	public void deleteModificationRequest(ModificationRequest request) {
		modificationRequestDao.remove(request);
		return;
	}

	/* (non-Javadoc)
	 * @see securbank.services.UserService#getUserByUsernameOrEmail(java.lang.String)
	 */
	@Override
	public User getUserByUsernameOrEmail(String email) {
		return userDao.findByUsernameOrEmail(email);
	}

	/* (non-Javadoc)
	 * @see securbank.services.UserService#getModificationRequestsByUsers(java.lang.String, java.lang.String, securbank.models.User)
	 */
	@Override
	public List<ModificationRequest> getModificationRequestsByUsers(String status, String type, List<User> users) {
		logger.info("Getting all modification request by user type, status of request and users");
		
		return modificationRequestDao.findAllbyStatusAndUserTypeAndUsers(status, type, users);

	}
	
	@Override
	public boolean verifyCurrentPassword(User user, String password) {
		if (BCrypt.checkpw(password, user.getPassword()))
			return true;
	
		return false;
	}
	
	@Override
	public User changeUserPassword(User user, ChangePasswordRequest model){
		user.setPassword(encoder.encode(model.getNewPassword()));			
		userDao.update(user);
		
		return user;			
	}

	@Override
	public User getUserByEmail(String email) {
		return userDao.findByUsernameOrEmail(email);
	}
	
	@Override
	public List<User> ListAllPII() {
		List<User> allPii = userDao.accessPii();
		return allPii;
	}
}