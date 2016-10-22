package securbank.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import securbank.models.ChangePasswordRequest;
import securbank.models.User;
import securbank.services.PkiService;
import securbank.services.UserService;
import securbank.validators.ChangePasswordFormValidator;
import securbank.validators.NewUserFormValidator;
/**
 * @author Ayush Gupta
 *
 */
@Controller
public class CommonController {
	@Autowired
	UserService userService;
	
	@Autowired 
	NewUserFormValidator userFormValidator;

	@Autowired 
	ChangePasswordFormValidator changePasswordFormValidator;
	

	final static Logger logger = LoggerFactory.getLogger(CommonController.class);
		
	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null){    
	        new SecurityContextLogoutHandler().logout(request, response, auth);
	    }
	    return "redirect:/login?logout";//You can redirect wherever you want, but generally it's a good practice to show login screen again.
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
		logger.info("Username: "+user.getUsername());
		
    	userService.createExternalUser(user);
    	
        return "redirect:/";
    }
	
	@GetMapping("/user/verify/{id}")
    public String verifyNewUser(Model model, @PathVariable UUID id) {
		if (userService.verifyNewUser(id) == false) {
			logger.info("GET request: verification failed of new external user");
			return "redirect:/error?code=400";
		}
		logger.info("GET request: verification of new external user");
		
		return "redirect:/";
    }
	
	@GetMapping("/request/verify/{id}")
    public String verifyEmailRequest(Model model, @PathVariable UUID id) {
		if (userService.verifyModificationRequest("waiting", id) == false) {
			logger.info("GET request: verification failed of request");
			return "redirect:/error?code=400&path=request-invalid";
		}
		logger.info("GET request: verification of request");
		
		return "redirect:/";
    }
	
	@GetMapping("/changepassword")
	public String ChangePasswordform(Model model){
		model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
		logger.info("GET request: Change password");
			
        return "changepassword";
	}
	
	@PostMapping("/changepassword")
    public String changeUserPassword(@ModelAttribute ChangePasswordRequest request, BindingResult binding) {
		changePasswordFormValidator.validate(request, binding);
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error?code=401";
		}
		if (!userService.verifyCurrentPassword(user, request.getExistingPassword())) {
			binding.rejectValue("existingPassword", "invalid.password", "Password is not valid");
		}
		if(binding.hasErrors()){
			logger.info("POST request: changepassword form with validation errors");
			return "changepassword";
		}			
		if(userService.changeUserPassword(user, request) != null){
			return "redirect:/login";
		}
		
		return "redirect:/error?code=500";
    }
}
