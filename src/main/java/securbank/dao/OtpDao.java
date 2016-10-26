package securbank.dao;

import java.util.List;
import java.util.UUID;

import securbank.models.Otp;
import securbank.models.User;

public interface OtpDao extends BaseDao<Otp, UUID>{
	public Otp findActiveOtpByUser(User user);
	public List<Otp> findAllOtpByUser(User user);
	public List<Otp> findAllActiveOtpByUser(User user);
}
