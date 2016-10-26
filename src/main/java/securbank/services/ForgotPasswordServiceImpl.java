package securbank.services;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import securbank.dao.UserDao;
import securbank.models.CreatePasswordRequest;
import securbank.models.ForgotPasswordRequest;
import securbank.models.LoginAttempt;
import securbank.models.User;
import securbank.models.Verification;

/**
 * 
 * @author Madhu
 *
 */

@Service("forgotPasswordService")
@Transactional
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private EmailService emailService;
	
	private SimpleMailMessage message;
	
	@Autowired
	private Environment env;
	
	final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Override
	public boolean sendEmailForgotPassword(Verification verification) {
		User user = verification.getUser();
		if (user == null)  {
			return false;
		}
		
		logger.info("Sending email for user to create new password");
		//setup up email message
		message = new SimpleMailMessage();
		message.setText(env.getProperty("forgot.password.body").replace(":id:",verification.getVerificationId().toString()));
		message.setSubject(env.getProperty("external.user.verification.subject"));
		message.setTo(user.getEmail());
		
		if(!emailService.sendEmail(message))
			return false;
		
		return true;
		
	}

	@Override
	public boolean verifyUserAndInfo(User user, ForgotPasswordRequest request) {
		logger.info("Verfying if the user deatilas and request details match");
		
		if(!(user.getEmail().equals(request.getEmail())) || !(user.getUsername().equals(request.getUserName())) )
			return false;
		
		return true;
	}
	
	/**
	 * Sets new password to the user 
	 * @param user user to assign the new password
	 * @param model model has the fields user enters while creating new password
	 */
	@Override
	public User createUserPassword(User user, CreatePasswordRequest model){
	 	user.setPassword(encoder.encode(model.getNewPassword()));
	 	//Reactivating locked account
	 	LoginAttempt attempt = user.getLoginAttempt();
	 	if(attempt.getCounter()>2 && user.getActive()==false){
	 		user.setActive(true);
	 		logger.info("Reactivating account: Setting active bit to 1");
	 	}
	 	userDao.update(user);	
	 	return user;			
	 }

	@Override
	public User getUserbyUsername(String username) {
		
		User user = userDao.findByUsernameOrEmail(username);
		if(user == null)
			return null;
		else
			return user;
	}

}
