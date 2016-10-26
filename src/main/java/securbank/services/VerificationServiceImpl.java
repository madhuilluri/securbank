/**
 * 
 */
package securbank.services;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import securbank.dao.VerificationDao;
import securbank.models.User;
import securbank.models.Verification;

/**
 * @author Ayush Gupta
 *
 */
@Service("verificationService")
@Transactional
public class VerificationServiceImpl implements VerificationService {

	@Autowired
	VerificationDao verificationDao;
	
	/* (non-Javadoc)
	 * @see securbank.services.VerificationService#getUserByIdAndType(java.util.UUID, java.lang.String)
	 */
	@Override
	public User getUserByIdAndType(UUID id, String type) {
		return verificationDao.findUserByIdAndType(id, type);
	}

	/* (non-Javadoc)
	 * @see securbank.services.VerificationService#createVerificationCodeByType(java.lang.String)
	 */
	@Override
	public Verification createVerificationCodeByType(User user, String type) {
		List<Verification> codes = verificationDao.findAllByUserAndType(user, type);
		
		for (Verification code : codes) {
			verificationDao.remove(code);
		}
		
		Verification verification = new Verification();
		verification.setCreatedOn(LocalDateTime.now());
		if (type.equalsIgnoreCase("lock")) {
			verification.setExpireOn(LocalDateTime.now().plusYears(1));
		}
		else {
			verification.setExpireOn(LocalDateTime.now().plusDays(1));
		}
		verification.setType(type);
		verification.setUser(user);
		verification = verificationDao.save(verification);
		
		return verification;
	}

	/* (non-Javadoc)
	 * @see securbank.services.VerificationService#removeVerification(java.util.UUID)
	 */
	@Override
	public void removeVerification(UUID id) {
		Verification verification = verificationDao.findById(id);
		if (verification == null) {
			return;
		}
		verificationDao.remove(verification);
		
		return;
	}
}
