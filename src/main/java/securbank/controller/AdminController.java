/**
 * 
 */
package securbank.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import securbank.exceptions.Exceptions;
import securbank.models.ModificationRequest;
import securbank.models.NewUserRequest;
import securbank.models.User;
import securbank.services.UserService;
import securbank.validators.ApprovalInternalUserFormValidator;
import securbank.validators.EditUserFormValidator;
import securbank.validators.NewUserRequestFormValidator;

/**
 * @author Ayush Gupta
 *
 */
@Controller
public class AdminController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private NewUserRequestFormValidator newUserRequestFormValidator;

	@Autowired
	private ApprovalInternalUserFormValidator approvalUserRequestFormValidator;

	@Autowired
	private EditUserFormValidator editUserFormValidator;

	final static Logger logger = LoggerFactory.getLogger(AdminController.class);

	@GetMapping("/admin/details")
	public String currentUserDetails(Model model) throws Exceptions {
		
		User user = userService.getCurrentUser();
		
		if (user == null) {
			logger.info("GET request: Unauthorized request for admin user detail");
			//return "redirect:/error?code=401";
			throw new Exceptions("401","Unauthorized Access !");
		}

		logger.info("GET request: Admin user detail");
		model.addAttribute("user", user);

		return "admin/detail";
	}

	@GetMapping("/admin/edit")
	public String editUser(Model model) throws Exceptions {
		User user = userService.getCurrentUser();
		
		if (user == null) {
			logger.info("GET request: Unauthorized request for admin user detail");
			//return "redirect:/error?code=401";
			throw new Exceptions("401"," ");
		}
		
		logger.info("GET request: Admin user detail");
		model.addAttribute("user", user);

		return "admin/edit";
	}
	
	@PostMapping("/admin/edit")
    public String editSubmit(@ModelAttribute User user, BindingResult bindingResult) throws Exceptions {
		User current = userService.getCurrentUser();
		
		if (user == null) {
			logger.info("GET request: Unauthorized request for admin user detail");
			//return "redirect:/error?code=401";
			throw new Exceptions("401"," ");
		}
		editUserFormValidator.validate(user, bindingResult);
		if (bindingResult.hasErrors()) {
			return "manager/edit";
        }
		user.setRole("ROLE_ADMIN");
		user.setUserId(current.getUserId());
		
		// create request
    	userService.editUser(user);
    	logger.info("POST request: Admin edit");
    	
        return "redirect:/admin/details?successEdit=true";
    }
	
	@GetMapping("/admin/user/add")
	public String signupForm(Model model, @RequestParam(required = false) Boolean success) {
		if (success != null) {
			model.addAttribute("success", success);
		}
		model.addAttribute("newUserRequest", new NewUserRequest());
		logger.info("GET request: Admin new user request");

		return "admin/newuserrequest";
	}

	@PostMapping("/admin/user/add")
	public String signupSubmit(@ModelAttribute NewUserRequest newUserRequest, BindingResult bindingResult) throws Exceptions {
		newUserRequestFormValidator.validate(newUserRequest, bindingResult);
		if (bindingResult.hasErrors()) {
			return "admin/newuserrequest";
        }
		if (userService.createNewUserRequest(newUserRequest) == null) {
			//return "redirect:/error?code=500";
			throw new Exceptions("500"," ");
		}

		logger.info("POST request: Admin new user request");

		return "redirect:/admin/user/add?success=true";
	}

	@GetMapping("/admin/user")
	public String getUsers(Model model) throws Exceptions {
		List<User> users = userService.getUsersByType("internal");
		if (users == null) {
			//return "redirect:/error?code=500";
			throw new Exceptions("500"," ");
		}
		model.addAttribute("users", users);
		logger.info("GET request: All internal users");

		return "admin/internalusers";
	}

	@GetMapping("/admin/user/edit/{id}")
	public String editUser(Model model, @PathVariable UUID id) throws Exceptions {
		User user = userService.getUserByIdAndActive(id);
		if (user == null) {
			//return "redirect:/error?code=404";
			throw new Exceptions("404"," ");
		}
		if (!user.getType().equals("internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","Unauthorized Request !");
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: All internal users");

		return "admin/internalusers_edit";
	}
	
	@PostMapping("/admin/user/edit/{id}")
    public String editSubmit(@ModelAttribute User user, @PathVariable UUID id, BindingResult bindingResult) throws Exceptions {
		User current = userService.getUserByIdAndActive(id);
		if (current == null) {
			//return "redirect:/error?code=404";
			throw new Exceptions("404"," ");
		}
		
		editUserFormValidator.validate(user, bindingResult);
		if (bindingResult.hasErrors()) {
			//return "redirect:/error?code=400?path=form-validation";
			throw new Exceptions("400","form.validation.Error");
        }
		if (!current.getType().equals("internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","request.unauthorized");
		}
		user.setUserId(id);
		logger.info("POST request: Internal user edit");
		user = userService.editUser(user);
		if (user == null) {
			//return "redirect:/error?code=500";
			throw new Exceptions("500"," ");
		}
		
        return "redirect:/admin/user?successEdit=true";
    }
	
	@GetMapping("/admin/user/delete/{id}")
	public String deleteUser(Model model, @PathVariable UUID id) throws Exceptions {
		User user = userService.getUserByIdAndActive(id);
		if (user == null) {
			//return "redirect:/error?code=404";
			throw new Exceptions("404"," ");
		}
		if (!user.getType().equals("internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","request.unauthorized");
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: Delete internal user");
		
		return "admin/internalusers_delete";
	}
	
	@PostMapping("/admin/user/delete/{id}")
    public String deleteSubmit(@ModelAttribute User user, @PathVariable UUID id, BindingResult bindingResult) throws Exceptions {
		User current = userService.getUserByIdAndActive(id);
		if (current == null) {
			//return "redirect:/error?code=404";
			throw new Exceptions("404"," ");
		}
		if (!current.getType().equals("internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","Unauthorized request !!");
		}
		
		userService.deleteUser(id);
		logger.info("POST request: Employee New modification request");
    	
        return "redirect:/admin/user?successDelete=true";
    }
	
	@GetMapping("/admin/user/{id}")
	public String getUserDetail(Model model, @PathVariable UUID id) throws Exceptions {
		User user = userService.getUserByIdAndActive(id);
		if (user == null) {
			//return "redirect:/error?code=404";
			throw new Exceptions("404"," ");
		}
		if (!user.getType().equals("internal")) {
			logger.warn("GET request: Unauthorized request for external user");

			//return "redirect:/error?code=409";
			throw new Exceptions("409"," ");
		}

		model.addAttribute("user", user);
		logger.info("GET request: Internal user details by id");
        	
        return "admin/userdetail";
    }
	
	@GetMapping("/admin/user/request")
    public String getAllUserRequest(Model model) {
		List<ModificationRequest> modificationRequests = userService.getModificationRequests("pending", "internal");
		if (modificationRequests == null) {
			model.addAttribute("modificationrequests", new ArrayList<ModificationRequest>());
		}
		else {
			model.addAttribute("modificationrequests", modificationRequests);	
		}
		logger.info("GET request: All user requests");
		
        return "admin/modificationrequests";
    }
	
	@GetMapping("/admin/user/request/view/{id}")
    public String getUserRequest(Model model, @PathVariable() UUID id) throws Exceptions {
		ModificationRequest modificationRequest = userService.getModificationRequest(id);
		
		if (modificationRequest == null) {
			//return "redirect:/error?code=404&path=request-invalid";
			throw new Exceptions("404","Invalid Request !");
		}
		if (!modificationRequest.getUserType().equals("internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","Unauthorised Request !");
		}
		model.addAttribute("modificationrequest", modificationRequest);
		logger.info("GET request: User modification request by ID");
		
        return "admin/modificationrequest_detail";
    }
	
	@PostMapping("/admin/user/request/{requestId}")
    public String approveEdit(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) throws Exceptions {
		String status = request.getStatus();
		if (status == null || !(request.getStatus().equals("approved") || request.getStatus().equals("rejected"))) {
			//return "redirect:/error?code=400&path=request-action-invalid";
			throw new Exceptions("400","Invalid Request Action !");
		}
		
		// checks validity of request
		if (userService.getModificationRequest(requestId) == null) {
			//return "redirect:/error?code=404&path=request-invalid";
			throw new Exceptions("404","Invalid Request !");
		}
		request.setModificationRequestId(requestId);
		approvalUserRequestFormValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			//return "redirect:/error?code=400&path=request-action-validation";
			throw new Exceptions("400","Invalid Request Action !");
		}
		
		// checks if admin is authorized for the request to approve
		if (!userService.verifyModificationRequestUserType(requestId, "internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","Unauthorised Request !");
		}
		
		request.setUserType("internal");
		request.setStatus(status);
		if (status.equals("approved")) {
			userService.approveModificationRequest(request);
		}
		// rejects request
		else {
			userService.rejectModificationRequest(request);
		}
		logger.info("POST request: Admin approves modification request");
		
        return "redirect:/admin/user/request?successAction=true";
    }	

	@GetMapping("/admin/user/request/delete/{id}")
    public String getDeleteRequest(Model model, @PathVariable() UUID id) throws Exceptions {
		ModificationRequest modificationRequest = userService.getModificationRequest(id);
		
		if (modificationRequest == null) {
			//return "redirect:/error?code=404&path=request-invalid";
			throw new Exceptions("404","Invalid Request !");
		}
		if (!modificationRequest.getUserType().equals("internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","Unauthorised Request !");
		}
		model.addAttribute("modificationrequest", modificationRequest);
		logger.info("GET request: User modification request by ID");
		
		
        return "admin/modificationrequest_delete";
    }
	
	@PostMapping("/admin/user/request/delete/{requestId}")
    public String deleteRequest(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) throws Exceptions {
		request = userService.getModificationRequest(requestId);
		
		// checks validity of request
		if (request == null) {
			//return "redirect:/error?code=404&path=request-invalid";
			throw new Exceptions("404","Invalid Request !");
		}
		
		if (!userService.verifyModificationRequestUserType(requestId, "internal")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			//return "redirect:/error?code=401&path=request-unauthorised";
			throw new Exceptions("401","Unauthorised Request !");
		}
		userService.deleteModificationRequest(request);
		logger.info("POST request: Admin approves modification request");
		
        return "redirect:/admin/user/request?successDelete=true";
    }	
	
	@RequestMapping("/admin/syslogs")
	public String adminControllerSystemLogs(Model model) {
		return "admin/systemlogs";
	}
	
	@GetMapping("/admin/logDownload")
    public void doDownload(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
		
		/**
	     * Size of a byte buffer to read/write file
	     */
	    final int BUFFER_SIZE = 4096;
	             
	    /**
	     * Path of the file to be downloaded, relative to application's directory
	     */
	    String filePath = "logs/application.log";
	     
 
        // get absolute path of the application
        ServletContext context = request.getServletContext();
        
        ClassLoader classLoader = getClass().getClassLoader();
 
        // construct the complete absolute path of the file
        File downloadFile = new File(classLoader.getResource(filePath).getFile());
        FileInputStream inputStream = new FileInputStream(downloadFile);
         
        // get MIME type of the file
        String mimeType = context.getMimeType("text/plain");
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);
 
        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());
 
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);
 
        // get output stream of the response
        OutputStream outStream = response.getOutputStream();
 
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
 
        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
 
        inputStream.close();
        outStream.close();
 
    }

}
