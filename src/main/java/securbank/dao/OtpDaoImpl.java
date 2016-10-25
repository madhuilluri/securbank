package securbank.dao;

import org.joda.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import securbank.models.Otp;
import securbank.models.User;

@Repository("otpDao")
public class OtpDaoImpl extends BaseDaoImpl<Otp, UUID> implements OtpDao{

	@Autowired
	EntityManager entityManager;
	
	@Override
	public Otp findActiveOtpByUser(User user) {
		try {
			return this.entityManager.createQuery("SELECT otp from Otp otp " +
					"WHERE otp.user = :user AND otp.active = :active " +
					"AND otp.expireOn > :now", Otp.class)
					.setParameter("user", user)
					.setParameter("active", true)
					.setParameter("now", LocalDateTime.now())
					.getSingleResult();	
		}
		catch(NoResultException e) {
			return null;
		}
	}

	@Override
	public List<Otp> findAllOtpByUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Otp> findAllActiveOtpByUser(User user) {
		return this.entityManager.createQuery("Select otp from Otp otp " +
				"WHERE otp.user = :user AND otp.active = :active " +
				"AND otp.expireOn > :now", Otp.class)
				.setParameter("user", user)
				.setParameter("active", true)
				.setParameter("now", LocalDateTime.now())
				.getResultList();
	}
}
