package securbank.services;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import securbank.dao.UserDao;

@SuppressWarnings("restriction")
@Service
public class PkiService {

	String TAG = this.getClass().getName() + " : ";

	@Autowired
	private UserDao userDAO;
	
	@Autowired
	private EmailService emailService;

	@SuppressWarnings("finally")
	@Transactional
	public String generatekeypair(String email) {

		KeyPairGenerator keysgenrated = null;
		String publickey = null;
		String privatekey = null;
		try {

			keysgenrated = KeyPairGenerator.getInstance("RSA");
			keysgenrated.initialize(2048);

			KeyPair pairs = keysgenrated.generateKeyPair();
			KeyFactory factory = KeyFactory.getInstance("RSA");

			X509EncodedKeySpec info_public = factory.getKeySpec(
					pairs.getPublic(), X509EncodedKeySpec.class);

			// create publicKey
			byte[] public_key = Base64.getEncoder().encode(info_public.getEncoded());
			publickey = public_key.toString();
			System.out.println( TAG +"Public Key : " + publickey);

			PKCS8EncodedKeySpec info_private = factory.getKeySpec(
					pairs.getPrivate(), PKCS8EncodedKeySpec.class);

			// create private key
			byte[] private_key = Base64.getEncoder().encode(info_private.getEncoded());
			privatekey = private_key.toString();
			System.out.println((TAG + "Private Key " + "\n" + privatekey));
			
			SimpleMailMessage message = new SimpleMailMessage();
			message.setText("Your private Key "  + privatekey);
			message.setSubject("Your private Key");
			message.setTo(email);
			
			emailService.sendEmail(message);
			
			System.out.println(TAG + " This is in key pair generation " + publickey);	

		} catch (Exception e) {
			System.out.println(e);
		}finally{
			return publickey;
		}
	}

	@Transactional
	public String paymentinfoencryption(String id, String oprivatekey) {

		// get corresponding userName of userID
		String userName = userDAO.findByUsernameOrEmail(id).getFirstName();
		System.out.println(userName);
		System.out.println(TAG+"userName : "  + userName);

		try {
			byte[] decoded  = Base64.getDecoder().decode(oprivatekey);

			PKCS8EncodedKeySpec info_private = new PKCS8EncodedKeySpec(decoded);

			KeyFactory factory = KeyFactory.getInstance("RSA");

			PrivateKey pkey = factory.generatePrivate(info_private);
			System.out.println(pkey.toString());

			Cipher generateCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			generateCipher.init(Cipher.ENCRYPT_MODE, pkey);
			//System.out.println(" 1 ");

			byte[] testvals = generateCipher.doFinal(Base64.getDecoder().decode(userName));
		
			byte[] encryptedString = Base64.getEncoder().encode(testvals);
			
			String encryptedUserName = new String(encryptedString);
			System.out.println(TAG+"encryptedUserName : " + encryptedUserName);
			return encryptedUserName;
			
		} catch (Exception e) {
			return null;
			//e.printStackTrace();
		}
	
	}
	
	@Transactional
	public boolean paymentinfodecryption(String user, String payment) {

	//	System.out.println(payment);
	//	System.out.println(" 1 ");
		if(payment==null){
			return false;
		}
		String user_publicKey = userDAO.findByUsernameOrEmail(user).getPublicKey();
		System.out.println(user_publicKey);

		byte[] decryption = null;
	
		try {
		
			decryption = Base64.getDecoder().decode(payment);
			
		} catch (Exception e) {
			System.out.println(TAG+" exception caught ");
			e.printStackTrace();
		}

		String userName = userDAO.findByUsernameOrEmail(user).getFirstName();
	

		try {
			

			byte[] arr = Base64.getDecoder().decode(user_publicKey);
	
			X509EncodedKeySpec info_public = new X509EncodedKeySpec(arr);
		
			KeyFactory factory = KeyFactory.getInstance("RSA");
			
			PublicKey publicKey = factory.generatePublic(info_public);
			

			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			
		

			byte[] usernamedecrypt = cipher.doFinal(decryption);
		

			String uname = (Base64.getDecoder().decode(usernamedecrypt)).toString();
			
			userName = userName.replaceAll("\\s+",""); 
			
			System.out.println(TAG+"uname is : " +uname + " userName is : " + userName);
			uname = uname.replaceAll("\\s+",""); 
			if (uname.equals(userName)) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
