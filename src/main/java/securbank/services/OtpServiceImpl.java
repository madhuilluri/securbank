package securbank.services;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import securbank.dao.OtpDao;
import securbank.models.Otp;
import securbank.models.User;

/**
 * @author Mitikaa
 *
 */

@Service("otpService")
@Transactional
public class OtpServiceImpl implements OtpService{

	@Autowired
	private OtpDao otpDao;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private EmailService emailService;
	
	private SimpleMailMessage message;

	final static Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);
	
	@Override
	public Otp createOtpForUser(User user) {
		logger.info("New OTP request");
		Otp otp = new Otp();
		otp.setUser(user);
		deactivateOtpByUser(user);
		otp = otpDao.save(otp);
		
		//send OTP email to user
		message = new SimpleMailMessage();
		message.setText("Your one time password is :" + otp.getCode());
		message.setSubject(env.getProperty("external.user.otp.subject"));
		message.setTo(user.getEmail());
		emailService.sendEmail(message);

		return otp;
	}

	@Override
	public Otp getOtpByUser(User user) {
		return otpDao.findActiveOtpByUser(user);
	} 
	
	public void deactivateOtpByUser(User user) {
		List<Otp> otps = otpDao.findAllActiveOtpByUser(user);
		for(Otp code : otps) {
			code.setActive(false);
			otpDao.update(code);
		}
	}
}
