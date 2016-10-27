package securbank.controller;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import securbank.dao.UserDao;
import securbank.exceptions.Exceptions;
import securbank.models.CreatePasswordRequest;
import securbank.models.ForgotPasswordRequest;
import securbank.models.Pii;
import securbank.models.ChangePasswordRequest;

import securbank.dao.UserDao;

import securbank.models.User;
import securbank.models.Verification;
import securbank.services.AuthenticationService;
import securbank.services.ForgotPasswordService;
import securbank.services.SecurityContextService;
import securbank.services.UserService;
import securbank.services.VerificationService;
import securbank.validators.ChangePasswordFormValidator;
import securbank.validators.CreatePasswordFormValidator;
import securbank.validators.NewUserFormValidator;

/**
 * @author Ayush Gupta
 *
 */
@Controller
public class CommonController {

	@Autowired
	AuthenticationService authSevice;

	@Autowired
	SecurityContextService securityContextService;

	@Autowired
	UserService userService;

	@Autowired
	NewUserFormValidator userFormValidator;
	
	@Autowired
	UserDao userDAO;
	
	@Autowired
	CreatePasswordFormValidator createPasswordFormValidator; 
	
	@Autowired
	VerificationService verificationService;
	
	@Autowired
    public HttpSession session;
	
	@Autowired
	private ForgotPasswordService forgotPasswordService;
	
	@Autowired
	ChangePasswordFormValidator changePasswordFormValidator;

	final static Logger logger = LoggerFactory.getLogger(CommonController.class);

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (securityContextService.isCurrentAuthenticationAnonymous()) {
			return "login";
		} else {
			String role = auth.getAuthorities().toString();
			if (role == null) {
				return "/";
			}
			return "redirect:" + authSevice.getRedirectUrlFromRole(role);
		}
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String loginPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/login?logout";// You can redirect wherever you want, but
									// generally it's a good practice to show
									// login screen again.
	}

	@GetMapping("/signup")
	public String signupForm(Model model) {
		model.addAttribute("user", new User());
		logger.info("GET request: signup form");

		return "signup";
	}

	@PostMapping("/signup")
	public String signupSubmit(@ModelAttribute User user, BindingResult bindingResult) {
		
		userFormValidator.validate(user, bindingResult);
		if (bindingResult.hasErrors()) {
			logger.info("POST request: signup form with validation errors");

			return "signup";
		}
		logger.info("POST request: signup");
		
		userService.createExternalUser(user);

		return "redirect:/";
	}

	@GetMapping("/verify/{id}")
	public String verifyNewUser(HttpServletResponse response, Model model, @PathVariable UUID id) throws Exceptions {
		if (userService.verifyNewUser(id) == false) {
			logger.info("GET request: verification failed of new external user");
			//return "redirect:/error?code=400";
			throw new Exceptions("400"," ");
		}
		
		logger.info("GET request: verification of new external user");
		Cookie cookie = new Cookie("flag", "true");
		cookie.setMaxAge(30*24*60*60 );
		response.addCookie(cookie);
		
		return "redirect:/";
    }
	

	// for forgot password
	@GetMapping("/forgotpassword")
	public String forgotpasswordform(Model model){
		model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
		logger.info("GET request : email address for forgot password");
		
		return "forgotpassword";
	}
	
	@PostMapping("/forgotpassword")
	public String forgotpasswordsubmit(@ModelAttribute ForgotPasswordRequest forgotPasswordRequest) throws Exceptions{
		User user = forgotPasswordService.getUserbyUsername(forgotPasswordRequest.getuserName());
		if(user == null) {
			logger.info("POST request: Forgot password with invalid user id");
			
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}
		
		if(!forgotPasswordService.verifyUserAndInfo(user, forgotPasswordRequest)) {
			logger.info("GET request : user and entered deails did not match");
			
			//return "redirect:/error?code=400&path-bad-request";
			throw new Exceptions("400","Bad Request !");
		}
		Verification verification = verificationService.createVerificationCodeByType(user, "forgot");	
		forgotPasswordService.sendEmailForgotPassword(verification);			
		logger.info("POST request : Sending link to reset password");
		
		return "redirect:/login";
	}
	
	@GetMapping("/createpassword/{id}")
	public String createpasswordform(Model model, @PathVariable UUID id) throws Exceptions{
		User user = verificationService.getUserByIdAndType(id, "forgot");
		if(user == null){
		
			logger.info("GET request : verification failed for user's registered email ");
			//return "redirect:/error?code=user.notfound";
			throw new Exceptions("400","User Not found!");
		}
		
		model.addAttribute("request", new CreatePasswordRequest());
		session.setAttribute("forgotpassword.verification", id);
		model.addAttribute("createPasswordRequest", new CreatePasswordRequest());
		logger.info("GET request : Create new password");
		return "createpassword";
	}
	
	
	@PostMapping("/createpassword")
    public String createPasswordSubmit(@ModelAttribute CreatePasswordRequest request, BindingResult binding) throws Exceptions {
		UUID token = (UUID) session.getAttribute("forgotpassword.verification");
		if (token == null) {
			logger.info("POST request: Email for forgot password with invalid session token");
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}

		// clears session
		User user = verificationService.getUserByIdAndType(token, "forgot");
		session.removeAttribute("forgotpassword.verification");
		
		// User user = userService.getUserByIdAndActive(token);
		
		if(user==null){
			logger.info("POST request: Forgot password with invalid user id");
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}
			
		verificationService.removeVerification(token);
		if(!user.getEmail().equals(request.getEmail()) || !user.getPhone().equals(request.getPhone()) ){
			logger.info("GET request : Creating new password with invalid credentials ");
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}
		
		createPasswordFormValidator.validate(request, binding);
			if(binding.hasErrors()){
				logger.info("POST request: createpassword form with validation errors");
				return "createpassword";
			}
			
		if(forgotPasswordService.createUserPassword(user, request) != null){
			return "redirect:/login";
		}
		//return "redirect:/error?code=500";
		throw new Exceptions("500"," ");

    }
	
	@GetMapping("/request/verify/{id}")
	public String verifyEmailRequest(Model model, @PathVariable UUID id) throws Exceptions {
		if (userService.verifyModificationRequest("waiting", id) == false) {
			logger.info("GET request: verification failed of request");
			//return "redirect:/error?code=400&path=request-invalid";
			throw new Exceptions("400","Bad Request !");
		}
		logger.info("GET request: verification of request");

		return "redirect:/";
	}

	@GetMapping("/changepassword")
	public String ChangePasswordform(Model model) {
		model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
		logger.info("GET request: Change password");

		return "changepassword";
	}

	@PostMapping("/changepassword")
	public String changeUserPassword(@ModelAttribute ChangePasswordRequest changePasswordRequest, BindingResult binding) throws Exceptions {
		logger.info("entered post request");
		changePasswordFormValidator.validate(changePasswordRequest, binding);
		if(binding.hasErrors()){
			logger.info("POST request: changepassword form with validation errors");
			return "changepassword";
		}
		User user = userService.getCurrentUser();
		if (user == null) {
			logger.info("user is null");
			//return "redirect:/error?code=401";
			throw new Exceptions("401"," ");
		}
		if (!userService.verifyCurrentPassword(user, changePasswordRequest.getExistingPassword())) {
			binding.rejectValue("existingPassword", "invalid.password", "Password is not valid");
		}
		if (binding.hasErrors()) {
			logger.info("POST request: changepassword form with validation errors");
			return "changepassword";
		}
		if (userService.changeUserPassword(user, changePasswordRequest) != null) {
			return "redirect:/login";
		}

		//return "redirect:/error?code=500";
		throw new Exceptions("500"," ");
	}
	
	@GetMapping("/reactivate/{id}")
	public String reactivateUser(Model model, @PathVariable UUID id) throws Exceptions{
		User user = verificationService.getUserByIdAndType(id, "lock");
		if (user == null) {
			logger.info("GET request: acoount reactivation failed");
			
			//return "redirect:/error?code=400";
			throw new Exceptions("400","Bad Request !");
		}
		session.setAttribute("reactivate.verification", id);
		model.addAttribute("createPasswordRequest", new CreatePasswordRequest());
		logger.info("GET request : reactivate account");
		return "reactivate";
	}
	
	
	@PostMapping("/reactivate")
    public String reactivateSubmit(@ModelAttribute CreatePasswordRequest request, BindingResult binding) throws Exceptions {
		UUID token = (UUID) session.getAttribute("reactivate.verification");
		if (token == null) {
			logger.info("POST request: Email for forgot password with invalid session token");
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}

		// clears session
		User user = verificationService.getUserByIdAndType(token, "lock");
		session.removeAttribute("reactivation.verification");
		
		// User user = userService.getUserByIdAndActive(token);
		if(user==null){
			logger.info("POST request: Forgot password with invalid user id");
			
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}
		verificationService.removeVerification(token);
		if(!user.getEmail().equals(request.getEmail()) || !user.getPhone().equals(request.getPhone()) ){
			logger.info("GET request : Creating new password with invalid credentials ");
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}
		
		createPasswordFormValidator.validate(request, binding);
			if(binding.hasErrors()){
				logger.info("POST request: createpassword form with validation errors");
				return "reactivate";
			}
		userService.verifyNewUser(user.getUserId());
		if(forgotPasswordService.createUserPassword(user, request) != null){
			return "redirect:/login";
		}
		
		//return "redirect:/error?code=500";
		throw new Exceptions("500"," ");
    }
	
}

