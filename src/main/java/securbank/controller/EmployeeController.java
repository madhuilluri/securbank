package securbank.controller;

import java.util.List;
import java.util.UUID;

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

import securbank.models.ModificationRequest;
import securbank.models.Transaction;
import securbank.models.Transfer;
import securbank.models.User;
import securbank.services.AccountService;
import securbank.services.TransactionService;
import securbank.services.TransferService;
import securbank.services.UserService;
import securbank.services.ViewAuthorizationService;
import securbank.validators.ApprovalUserFormValidator;
import securbank.validators.EditUserFormValidator;
import securbank.validators.InternalEditUserFormValidator;

/**
 * @author Ayush Gupta
 *
 */
@Controller
public class EmployeeController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private TransferService transferService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private InternalEditUserFormValidator editUserFormValidator;

	@Autowired
	private EditUserFormValidator editExternalUserFormValidator;
	
	@Autowired
	private ApprovalUserFormValidator approvalUserFormValidator;
	
	@Autowired
	private ViewAuthorizationService viewAuthorizationService;

	final static Logger logger = LoggerFactory.getLogger(EmployeeController.class);
	
	@GetMapping("/employee/details")
    public String currentUserDetails(Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error?code=404&path=user-notfound";
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: Employee user detail");
		
        return "employee/detail";
    }
	
	@GetMapping("/employee/edit")
    public String editUser(Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error";
		}
		model.addAttribute("user", user);
		logger.info("GET request: Employee profile edit");
		
        return "employee/edit";
    }
	
	@PostMapping("/employee/edit")
    public String editSubmit(@ModelAttribute User user, BindingResult bindingResult) {
		editUserFormValidator.validate(user, bindingResult);
		if (bindingResult.hasErrors()) {
			return "employee/edit";
        }
		
		// create request
    	userService.createInternalModificationRequest(user);
    	logger.info("POST request: Employee New modification request");
    	
        return "redirect:/employee/details?successEdit=true";
    }
	
	@GetMapping("/employee/user/request")
    public String getAllUserRequest(Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error:/login";
		}
		
		List<User> users = viewAuthorizationService.getAllAuthorization(user);
		List<ModificationRequest> modificationRequests = userService.getModificationRequestsByUsers("pending", "external", users);
		
		model.addAttribute("modificationrequests", modificationRequests);	
		logger.info("GET request: Employee All external modification requests");
		
        return "employee/modificationrequests";
    }
	
	@GetMapping("/employee/user/request/view/{id}")
    public String getUserRequest(Model model, @PathVariable() UUID id) {
		ModificationRequest modificationRequest = userService.getModificationRequest(id);
		
		if (modificationRequest == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/login";
		}
		if (!viewAuthorizationService.hasAccess(user, modificationRequest.getUser())) {
			return "redirect:/error?code=401";
		}
		model.addAttribute("modificationrequest", modificationRequest);
		logger.info("GET request: Employee external modification request by ID");
		
        return "employee/modificationrequest_detail";
    }
	
	@PostMapping("/employee/user/request/{requestId}")
    public String approveEdit(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) {
		String status = request.getStatus();
		if (status == null || !(request.getStatus().equals("approved") || request.getStatus().equals("rejected"))) {
			return "redirect:/error?code=400&path=request-action-invalid";
		}
		
		// checks validity of request
		if (userService.getModificationRequest(requestId) == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		request.setModificationRequestId(requestId);
		approvalUserFormValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			return "redirect:/error?code=400&path=request-action-validation";
		}
		ModificationRequest modificationRequest = userService.getModificationRequest(requestId);
		if (modificationRequest == null) {
			return "redirect:/error?code=400&path=request-action-validation";
		}
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/login";
		}
		if (!viewAuthorizationService.hasAccess(user, modificationRequest.getUser())) {
			return "redirect:/error?code=401";
		}
		// checks if employee is authorized for the request to approve
		if (!userService.verifyModificationRequestUserType(requestId, "external")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		else {
			request.setUserType("external");
		}
		
		request.setStatus(status);
		if (status.equals("approved")) {
			userService.approveModificationRequest(request);
		}
		// rejects request
		else {
			userService.rejectModificationRequest(request);
		}
		logger.info("POST request: Employee approves external modification request");
		
        return "redirect:/employee/user/request?successAction=true";
    }
	
	@GetMapping("/employee/user/request/delete/{id}")
    public String deleteRequest(Model model, @PathVariable() UUID id) {
		ModificationRequest modificationRequest = userService.getModificationRequest(id);
		
		if (modificationRequest == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/login";
		}
		if (!viewAuthorizationService.hasAccess(user, modificationRequest.getUser())) {
			return "redirect:/error?code=401";
		}
		
		model.addAttribute("modificationrequest", modificationRequest);
		logger.info("GET request: Employee external modification request by ID");
		
        return "employee/modificationrequest_delete";
    }
	
	@PostMapping("/employee/user/request/delete/{requestId}")
    public String deleteRequest(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) {
		request = userService.getModificationRequest(requestId);
		
		// checks validity of request
		if (request == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/login";
		}
		if (!viewAuthorizationService.hasAccess(user, request.getUser())) {
			return "redirect:/error?code=401";
		}
		
		// checks if admin is authorized for the request to approve
		if (!request.getUserType().equals("external")) {
			logger.warn("GET request: Employee unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		userService.deleteModificationRequest(request);
		logger.info("POST request: Employee approves modification request");
		
        return "redirect:/employee/user/request?successDelete=true";
    }	
	
	@GetMapping("/employee/user")
    public String getUsers(Model model) {
		List<User> users = userService.getUsersByType("external");
		if (users == null) {
			return "redirect:/error?code=500";
		}
		model.addAttribute("users", users);
		logger.info("GET request:  All external users");
		
        return "employee/externalusers";
    }
	
	@GetMapping("/employee/user/{id}")
    public String getUserDetails(Model model, @PathVariable UUID id) {
		User user = userService.getUserByIdAndActive(id);
		User current = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error?code=404";
		}
		if (current == null) {
			return "redirect:/login";
		}
		if (!user.getType().equals("external")) {
			logger.warn("GET request: Unauthorised request for external user detail");
			
			return "redirect:/error?code=401";
		}
		if (!viewAuthorizationService.hasAccess(current, user)) {
			return "redirect:/error?code=401";
		}
		model.addAttribute("user", user);
		logger.info("GET request:  External user detail by id");
		
        return "employee/userdetail";
    }
	
	@GetMapping("/employee/user/edit/{id}")
	public String editUser(Model model, @PathVariable UUID id) {
		User user = userService.getUserByIdAndActive(id);
		User current = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error?code=404";
		}
		if (current == null) {
			return "redirect:/login";
		}
		if (!user.getType().equals("external")) {
			logger.warn("GET request: Unauthorised request for external user edit");
			
			return "redirect:/error?code=401";
		}
		if (!viewAuthorizationService.hasAccess(current, user)) {
			return "redirect:/error?code=401";
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: All external users");

		return "employee/externalusers_edit";
	}
	
	@PostMapping("/employee/user/edit/{id}")
    public String editSubmit(@ModelAttribute User user, @PathVariable UUID id, BindingResult bindingResult) {
		User current = userService.getUserByIdAndActive(id);
		User employee = userService.getCurrentUser();
		if (employee == null) {
			return "redirect:/login";
		}
		if (current == null) {
			return "redirect:/error?code=404";
		}
		
		editExternalUserFormValidator.validate(user, bindingResult);
		if (bindingResult.hasErrors()) {
			return "redirect:/error?code=400?path=form-validation";
        }
		if (!current.getType().equals("external")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		if (!viewAuthorizationService.hasAccess(employee, current)) {
			return "redirect:/error?code=401";
		}
		
		user.setUserId(id);
		logger.info("POST request: Internal user edit");
		user = userService.editUser(user);
		if (user == null) {
			return "redirect:/error?code=500";
		}
		
        return "redirect:/employee/user?successEdit=true";
    }
	
	@GetMapping("/employee/user/delete/{id}")
	public String deleteUser(Model model, @PathVariable UUID id) {
		User user = userService.getUserByIdAndActive(id);
		User current = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error?code=404";
		}
		if (current == null) {
			return "redirect:/login";
		}
		if (!user.getType().equals("external")) {
			logger.warn("GET request: Unauthorised request for external user edit");
			
			return "redirect:/error?code=401";
		}
		if (!viewAuthorizationService.hasAccess(current, user)) {
			return "redirect:/error?code=401";
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: Delete external user");
		
		return "employee/externalusers_delete";
	}
	
	@PostMapping("/employee/user/delete/{id}")
    public String deleteSubmit(@ModelAttribute User user, @PathVariable UUID id, BindingResult bindingResult) {
		User current = userService.getUserByIdAndActive(id);
		if (current == null) {
			return "redirect:/error?code=404";
		}
		if (!current.getType().equals("external")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		User employee = userService.getCurrentUser();
		if (employee == null) {
			return "redirect:/login";
		}
		if (!viewAuthorizationService.hasAccess(employee, current)) {
			return "redirect:/error?code=401";
		}
		userService.deleteUser(id);
		logger.info("POST request: Employee New modification request");
    	
        return "redirect:/employee/user?successDelete=true";
    }
	
	// View Authorization Start
	@GetMapping("/employee/user/requestaccess")
    public String addViewRequest(Model model) {
		model.addAttribute("user", new User());
		logger.info("GET request: Request for user");
		
        return "employee/requestaccess";
    }
	
	@PostMapping("/employee/user/requestaccess")
    public String addViewRequest(@ModelAttribute User user, BindingResult bindingResult) {
		if (user.getEmail() == null || user.getEmail().equals("")) {
			bindingResult.rejectValue("email", "email.invalid", "Email not valid");
		}
		user = userService.getUserByUsernameOrEmail(user.getEmail());
		if (user == null || !user.getType().equalsIgnoreCase("external")) {
			bindingResult.rejectValue("email", "email.invalid", "Email not valid");
		}
		
		if (viewAuthorizationService.hasAccess(userService.getCurrentUser(), user)) {
			bindingResult.rejectValue("email", "email.invalid", "Authorization exists");
		}
		
		if (bindingResult.hasErrors()) {
			return "employee/requestaccess";
		}
		viewAuthorizationService.createAuthorization(userService.getCurrentUser(), user, false);
		logger.info("POST request: Employee request access");
		
        return "redirect:/employee/user/requestaccess?success=true";
    }	
	
	
	@GetMapping("/employee/transactions")
    public String getTransactions(Model model) {

		List<Transaction> transactions = transactionService.getTransactionsByStatus("Pending");
		if (transactions == null) {
			return "redirect:/error?code=500";
		}
		model.addAttribute("transactions", transactions);
		logger.info("GET request:  All pending transactions");
		
        return "employee/pendingtransactions";
    }
	
	@GetMapping("/employee/transaction/{id}")
    public String getTransactionRequest(Model model, @PathVariable() UUID id) {
		Transaction transaction = transactionService.getTransactionById(id);
		
		if (transaction == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}

		// checks if employee is authorized for the request to approve
		if (!transaction.getAccount().getUser().getType().equals
				("external")) {
			logger.warn("GET request: Employee unauthrorised request access");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		model.addAttribute("transaction", transaction);
		logger.info("GET request: Employee external transaction request by ID");
		
        return "employee/approvetransaction";
    }
	
	@PostMapping("/employee/transaction/request/{id}")
    public String approveRejectTransactions(@ModelAttribute Transaction trans, @PathVariable() UUID id, BindingResult bindingResult) {
		
		Transaction transaction = transactionService.getTransactionById(id);
		if (transaction == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		
		// checks if employee is authorized for the request to approve
		if (!transaction.getAccount().getUser().getType().equalsIgnoreCase
				("external")) {
			logger.warn("GET request: Employee unauthrorised request access");
					
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		if("approved".equalsIgnoreCase(trans.getApprovalStatus())){
			if(transactionService.isTransactionValid(transaction)==false && transaction.getType().equals("DEBIT")){
				return "redirect:/error?code=404&path=amount-invalid";
			}
			transactionService.approveTransaction(transaction);
		}
		else if ("rejected".equalsIgnoreCase(trans.getApprovalStatus())) {
			transactionService.declineTransaction(transaction);
		}
		
		logger.info("GET request: Employee approve/decline external transaction requests");
		
        return "redirect:/employee/transactions?successAction=true";
    }
	
	@GetMapping("/employee/transfers")
    public String getTransfers(Model model) {
		logger.info("GET request:  All pending transfers");
		
		List<Transfer> transfers = transferService.getTransfersByStatus("Pending");
		if (transfers == null) {
			return "redirect:/error?code=500";
		}
		model.addAttribute("transfers", transfers);
		
        return "employee/pendingtransfers";
    }
	
	@GetMapping("/employee/transfer/{id}")
    public String getTransferRequest(Model model, @PathVariable() UUID id) {
		Transfer transfer = transferService.getTransferById(id);
		
		if (transfer == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}

		// checks if employee is authorized for the request to approve
		if (!transfer.getToAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made TO non external account");		
			return "redirect:/error?code=401&path=request-unauthorised";
		}
				
		if (!transfer.getFromAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made FROM non external account");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
				
		model.addAttribute("transfer", transfer);
		logger.info("GET request: Employee external transfer request by ID");
		
        return "employee/approvetransfer";
	}
	
	@PostMapping("/employee/transfer/request/{id}")
    public String approveRejectTransfer(@ModelAttribute Transfer trans, @PathVariable() UUID id, BindingResult bindingResult) {
		
		Transfer transfer = transferService.getTransferById(id);
		if (transfer == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		
		//give error if account does not exist
		if (!accountService.accountExists(transfer.getToAccount())) {
			logger.warn("TO account does not exist");	
			return "redirect:/error?code=401&path=request-invalid";
		}
		
		// checks if employee is authorized for the request to approve
		if (!transfer.getToAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made TO non external account");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		if (!transfer.getFromAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made FROM non external account");
					
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		if("approved".equalsIgnoreCase(trans.getStatus())){
			if(transferService.isTransferValid(transfer)==false){
				return "redirect:/error?code=404&path=amount-invalid";
			}
			transferService.approveTransfer(transfer);
		}
		else if ("rejected".equalsIgnoreCase(trans.getStatus())) {
			transferService.declineTransfer(transfer);
		}
		
		logger.info("GET request: Employee approve/decline external transaction requests");
		
        return "redirect:/employee/transfers?successAction=true";
    }
}
