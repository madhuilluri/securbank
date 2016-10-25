package securbank.services;

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
import securbank.models.User;

/**
 * 
 * @author Madhu
 *
 */

@Service("forgotPasswordService")
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
	public boolean sendEmailForgotPassword(User user) {
		
		logger.info("Sending email for user to create new password");
		//setup up email message
		message = new SimpleMailMessage();
		message.setText(env.getProperty("forgot.password.body").replace(":id:",user.getUserId().toString()));
		message.setSubject(env.getProperty("external.user.verification.subject"));
		message.setTo(user.getEmail());
		
		if(!emailService.sendEmail(message))
			return false;
		
		return true;
		
	}

	@Override
	public boolean verifyUserAndInfo(User user, ForgotPasswordRequest request) {
		logger.info("Verfying if the user deatilas and request details match");
		
		if(!(user.getEmail().equals(request.getEmail())) || !(user.getUsername().equals(request.getUsername())) )
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
