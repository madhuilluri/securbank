package securbank.services;

import javax.servlet.http.Cookie;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import securbank.dao.UserDao;
import securbank.models.LoginAttempt;
import securbank.models.User;
import securbank.models.Verification;

/**
 * @author Ayush Gupta
 *
 */
@Transactional
@Service("authenticationService")
public class AuthenticationServiceImpl implements AuthenticationService {
	
	@Autowired
	private UserDao userDao;

	
	@Autowired 
	private PasswordEncoder encoder;
	
	@Autowired
	private EmailService emailService;
	
	private SimpleMailMessage message;
	
	@Autowired
	private Environment env;
	
	@Autowired
	VerificationService verificationService;
	
	@Autowired
	private ForgotPasswordService forgotPasswordService;

	@Autowired
	private UserService userService;
	
	private Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	/**
     * Verify the username and password for current user
     * 
     * @param usernane
     *            The username or email of user
     * @param password
     *            The password of user
     * @return user
     */
	@Override
	public User verifyUser(String username, String password) {
		User user = userDao.findByUsernameOrEmail(username);
		if (user == null) {
			return null;
		}
		
		logger.info("Verifying username and password");
		
		if (!BCrypt.checkpw(password, user.getPassword())) {
			//User found-Incorrect password, counter incremented 
			LoginAttempt attempt= user.getLoginAttempt();
			attempt.setCounter(attempt.getCounter() + 1);
			if(attempt.getCounter() == 3){
				user.setActive(false);
				
				
				Verification verification = verificationService.createVerificationCodeByType(user, "lock");				
				if (verification != null) {
					// Send email message
					message = new SimpleMailMessage();
					message.setText(env.getProperty("account.reactivate.body").replace(":id:",verification.getVerificationId().toString()));
					message.setSubject(env.getProperty("account.reactivate.subject"));
					message.setTo(user.getEmail());
					emailService.sendEmail(message);
					logger.info("POST request : Sending link to reset password and reactivate account.");
				}
			}
			user.setLoginAttempt(attempt);
			userDao.update(user);
			return null;
		}
		
		return user;
	}
	
	
	/**
     * Updates the last login time of user
     * 
     * @param user
     *            The User object of user
     * @return user
     */
	@Override
	public User updateLoginTime(User user) {
		user.setLastLogin(LocalDateTime.now());
		//Login Successful, LoginAttempt counter reset to 0
		LoginAttempt attempt = user.getLoginAttempt();
		if(attempt==null)
			attempt=new LoginAttempt(user, 0, LocalDateTime.now());
		attempt.setCounter(0);
		attempt.setLastUpdated(LocalDateTime.now());
		user.setLoginAttempt(attempt);

		logger.info("Resetting LoginAteempt counter to 0");
		
		user = userDao.update(user);
		if (user == null) {
			return null;
		}
		logger.info("Updating login time for User");
		
		return user;
	}


	@Override
	public String getRedirectUrlFromRole(String role) {
		// TODO Auto-generated method stub
		String targetUrl = "";
        if(role.contains("ADMIN")) {
            targetUrl = "/admin/details";
        } else if(role.contains("MANAGER")) {
            targetUrl = "/manager/details";
        } else if(role.contains("EMPLOYEE")) {
            targetUrl = "/employee/details";
        } else if(role.contains("INDIVIDUAL")) {
            targetUrl = "/user/details";
        } else if(role.contains("MERCHANT")) {
            targetUrl = "/merchant/details";
        }
        return targetUrl;
	}

	public Cookie validateCookie(Cookie[] cookies, String username) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return null;
		}
		Cookie active = null;
        if (cookies != null) {
        	for (Cookie cookie : cookies) {
        		if (cookie.getName().equals("flag")) {
        			active = cookie;
        			break;
        		}
        	}
        }
        if (active == null) {
        	message = new SimpleMailMessage();
        	message.setText(env.getProperty("suspicious.login.body"));
			message.setSubject(env.getProperty("suspicious.login.subject"));
			message.setTo(user.getEmail());
			emailService.sendEmail(message);
        }
        
        return active;
	}
}
