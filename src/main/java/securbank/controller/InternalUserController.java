package securbank.controller;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import securbank.exceptions.Exceptions;
import securbank.models.NewUserRequest;
import securbank.models.User;
import securbank.services.UserService;
import securbank.validators.NewInternalUserFormValidator;

/**
 * @author Ayush Gupta
 *
 */
@Controller
public class InternalUserController {
	@Autowired
	private UserService userService;
	
	@Autowired 
	NewInternalUserFormValidator userFormValidator;
	
	@Autowired
    public HttpSession session;
	
	final static Logger logger = LoggerFactory.getLogger(InternalUserController.class);
	
	@GetMapping("/internal/user/verify/{id}")
    public String verifyNewUser(Model model, @PathVariable UUID id) throws Exceptions {
		NewUserRequest newUserRequest = userService.getNewUserRequest(id);
		if (newUserRequest == null) {
			logger.info("GET request: Invalid verfication for new user");
			
			//return "redirect/error?code=400&path=no-request";
			throw new Exceptions("400","No Request !");
		}
		
		logger.info("GET request: Verify new internal user");
		session.setAttribute("verification.token", newUserRequest.getNewUserRequestId());
		User user = new User();
		user.setEmail(newUserRequest.getEmail());
		user.setRole(newUserRequest.getRole());
		model.addAttribute("user", user);
		
		return "internal/signup";
    }
	
	@PostMapping("/internal/user/signup")
    public String internalSignupSubmit(HttpServletResponse response, @ModelAttribute User user, BindingResult bindingResult) throws Exceptions {
    	UUID token = (UUID) session.getAttribute("verification.token");
		if (token == null) {
			logger.info("POST request: Signup internal user with invalid session token");
			
			//return "redirect:/error?code=400&path=bad-request";
			throw new Exceptions("400","Bad Request !");
		}
		else {
			// clears session
			session.removeAttribute("validation.token");
		}
		logger.info("POST request: Signup internal user");
		NewUserRequest newUserRequest = userService.getNewUserRequest(token);
		if (newUserRequest == null) {
			logger.info("POST request: Signup internal user with invalid id");
			
			//return "redirect:/error?code=400&path=token-invalid";
			throw new Exceptions("400","Invalid Token !");
		}
		if(!newUserRequest.getEmail().equals(user.getEmail()) || !newUserRequest.getRole().equals(user.getRole())) {
			logger.info("GET request: Signup internal user with invalid credentials");
			
			//return "redirect:/error?code=400";
			throw new Exceptions("400"," ");
		}
		userFormValidator.validate(user, bindingResult);
		
		if (bindingResult.hasErrors()) {
			return "internal/signup";
        }
		
		if (userService.createInternalUser(user) == null) {
			//return "redirect:/error?code=400&path=user-invalid";
			throw new Exceptions("400","User Invalid !");
		};
    	
		Cookie cookie = new Cookie("flag", "true");
		cookie.setMaxAge(30*24*60*60);
		response.addCookie(cookie);
		
        return "redirect:/login";
    }
}
