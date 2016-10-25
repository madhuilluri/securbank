/**
 * 
 */
package securbank.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;

import securbank.models.ModificationRequest;
import securbank.models.Transaction;
import securbank.models.Transfer;
import securbank.models.User;
import securbank.models.ViewAuthorization;
import securbank.services.AccountService;
import securbank.services.TransactionService;
import securbank.services.TransferService;
import securbank.services.UserService;
import securbank.services.ViewAuthorizationService;
import securbank.validators.ApprovalUserFormValidator;
import securbank.validators.AuthorizeUserFormValidator;
import securbank.validators.EditUserFormValidator;
import securbank.validators.InternalEditUserFormValidator;

/**
 * @author Ayush Gupta
 *
 */
@Controller
public class ManagerController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private TransferService transferService;
	
	@Autowired
	private AccountService accountService;
	
	private InternalEditUserFormValidator editUserFormValidator;

	@Autowired
	private EditUserFormValidator editExternalUserFormValidator;

	@Autowired
	private ApprovalUserFormValidator approvalUserFormValidator;

	@Autowired
	private AuthorizeUserFormValidator authorizeUserFormValidator;

	@Autowired
	private ViewAuthorizationService viewAuthorizationService;

	final static Logger logger = LoggerFactory.getLogger(ManagerController.class);
	
	@GetMapping("/manager/details")
    public String currentUserDetails(Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {

			return "redirect:/error?code=400&path=user-notfound";
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: Manager user detail");
			
        return "manager/detail";
    }
	
	@GetMapping("/manager/edit")
    public String editUser(Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error";
		}
		model.addAttribute("user", user);
		logger.info("GET request: Manager profile edit");
		
        return "manager/edit";
    }
	
	@PostMapping("/manager/edit")
    public String editSubmit(@ModelAttribute User user, BindingResult bindingResult) {
		editUserFormValidator.validate(user, bindingResult);
		if (bindingResult.hasErrors()) {
			return "manager/edit";
        }
		
		// create request
    	userService.createInternalModificationRequest(user);
    	logger.info("POST request: Manager New modification request");
    	
        return "redirect:/";
    }
	
	@GetMapping("/manager/user/request")
    public String getAllUserRequest(Model model) {
		List<ModificationRequest> modificationRequests = userService.getModificationRequests("pending", "external");
		if (modificationRequests == null) {
			model.addAttribute("modificationrequests", new ArrayList<ModificationRequest>());
		}
		else {
			model.addAttribute("modificationrequests", modificationRequests);	
		}
		logger.info("GET request: Manager All external modification requests");
		
        return "manager/modificationrequests";
    }
	
	@GetMapping("/manager/user/request/view/{id}")
    public String getUserRequest(Model model, @PathVariable() UUID id) {
		ModificationRequest modificationRequest = userService.getModificationRequest(id);
		
		if (modificationRequest == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}

		// checks if manager is authorized for the request to approve
		if (!modificationRequest.getUserType().equals("external")) {
			logger.warn("GET request: Manager unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		model.addAttribute("modificationrequest", modificationRequest);
		logger.info("GET request: Manager external modification request by ID");
		
        return "manager/modificationrequest_detail";
    }
	
	@PostMapping("/manager/user/request/{requestId}")
    public String approveEdit(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) {
		String status = request.getStatus();
		if (status == null || !(request.getStatus().equals("approved") || request.getStatus().equals("rejected"))) {
			return "redirect:/error?code=400&path=request-action-invalid";
		}
		
		if (userService.getModificationRequest(requestId) == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}

		request.setModificationRequestId(requestId);
		approvalUserFormValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			return "redirect:/error?code=400&path=request-action-validation";
		}
		
		// checks if manager is authorized for the request to approve
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
		
        return "redirect:/manager/user/request";
	}

	@GetMapping("/manager/user")
    public String getUsers(Model model) {
		List<User> users = userService.getUsersByType("external");
		if (users == null) {
			return "redirect:/error?code=500";
		}
		model.addAttribute("users", users);
		logger.info("GET request:  All external users");

		return "manager/externalusers";
    }
	
	@GetMapping("/manager/user/{id}")
    public String getUserDetails(Model model, @PathVariable UUID id) {
		User user = userService.getUserByIdAndActive(id);
		if (user == null) {
			return "redirect:/error?code=400";
		}
		if (!user.getType().equals("external")) {
			logger.warn("GET request: Unauthorised request for external user detail");
			
			return "redirect:/error?code=401";
		}
		
		model.addAttribute("user", user);
		logger.info("GET request:  External user detail by id");
		
        return "manager/userdetail";
    }
	
	@GetMapping("/manager/transactions")
    public String getTransactions(Model model) {

		List<Transaction> transactions = transactionService.getTransactionsByStatus("Pending");
		if (transactions == null) {
			return "redirect:/error?code=500";
		}
		model.addAttribute("transactions", transactions);
		logger.info("GET request:  All pending transactions");
		
        return "manager/pendingtransactions";
    }
	
	@GetMapping("/manager/transaction/{id}")
    public String getTransactionRequest(Model model, @PathVariable() UUID id) {
		Transaction transaction = transactionService.getTransactionById(id);
		
		if (transaction == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}

		// checks if manager is authorized for the request to approve
		if (!transaction.getAccount().getUser().getType().equals
				("external")) {
			logger.warn("GET request: Manager unauthrorised request access");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		model.addAttribute("transaction", transaction);
		logger.info("GET request: Manager external transaction request by ID");
		
        return "manager/approvetransaction";
    }
	
	@PostMapping("/manager/transaction/request/{id}")
    public String approveRejectTransactions(@ModelAttribute Transaction trans, @PathVariable() UUID id, BindingResult bindingResult) {
		
		Transaction transaction = transactionService.getTransactionById(id);
		if (transaction == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		
		// checks if manager is authorized for the request to approve
		if (!transaction.getAccount().getUser().getType().equalsIgnoreCase
				("external")) {
			logger.warn("GET request: Manager unauthrorised request access");
					
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
		
		logger.info("GET request: Manager approve/decline external transaction requests");
		
        return "redirect:/manager/transactions";
    }
			
	@GetMapping("/manager/user/edit/{id}")
	public String editUser(Model model, @PathVariable UUID id) {
		User user = userService.getUserByIdAndActive(id);
		if (user == null) {
			return "redirect:/error?code=404";
		}
		if (!user.getType().equals("external")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		model.addAttribute("user", user);
		logger.info("GET request: All external users");

		return "manager/externalusers_edit";
	}
	
	@PostMapping("/manager/user/edit/{id}")
    public String editSubmit(@ModelAttribute User user, @PathVariable UUID id, BindingResult bindingResult) {
		User current = userService.getUserByIdAndActive(id);
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
		user.setUserId(id);
		logger.info("POST request: Internal user edit");
		user = userService.editUser(user);
		if (user == null) {
			return "redirect:/error?code=500";
		}
		
        return "redirect:/manager/user";
    }
	
	@GetMapping("/manager/user/delete/{id}")
	public String deleteUser(Model model, @PathVariable UUID id) {
		User user = userService.getUserByIdAndActive(id);
		if (user == null) {
			return "redirect:/error?code=404";
		}
		if (!user.getType().equals("external")) {
			logger.warn("GET request: Admin unauthrorised request access");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
			model.addAttribute("user", user);
			logger.info("GET request: Delete external user");
			
			return "manager/externalusers_delete";
	}
		
	
	@GetMapping("/manager/transfers")
    public String getTransfers(Model model) {
		logger.info("GET request:  All pending transfers");
		
		List<Transfer> transfers = transferService.getTransfersByStatus("Pending");
		if (transfers == null) {
			return "redirect:/error?code=500";
		}
		model.addAttribute("transfers", transfers);
		
        return "manager/pendingtransfers";
    }
	
	@PostMapping("/manager/transfer/request/{id}")
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
		
		// checks if manager is authorized for the request to approve
		if (!transfer.getToAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made TO non external account");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		if (!transfer.getFromAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made FROM non external account");
					
			return "redirect:/error?code=401&path=request-unauthorised";
		}

		if("approved".equalsIgnoreCase(trans.getStatus())){
			//check if transfer is valid in case modified
			if(transferService.isTransferValid(transfer)==false){
				return "redirect:/error?code=401&path=amount-invalid";
			}
			transferService.approveTransfer(transfer);
		}
		else if ("rejected".equalsIgnoreCase(trans.getStatus())) {
			transferService.declineTransfer(transfer);
		}
		
		logger.info("GET request: Manager approve/decline external transaction requests");
		
        return "redirect:/manager/transfers";
    }
	
	@GetMapping("/manager/transfer/{id}")
    public String getTransferRequest(Model model, @PathVariable() UUID id) {
		Transfer transfer = transferService.getTransferById(id);
		
		if (transfer == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}

		// checks if manager is authorized for the request to approve
		if (!transfer.getToAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made TO non external account");		
			return "redirect:/error?code=401&path=request-unauthorised";
		}
				
		if (!transfer.getFromAccount().getUser().getType().equalsIgnoreCase("external")) {
			logger.warn("Transafer made FROM non external account");
			return "redirect:/error?code=401&path=request-unauthorised";
		}
				
		model.addAttribute("transfer", transfer);
		logger.info("GET request: Manager external transfer request by ID");
		
        return "manager/approvetransfer";
	}
			
	@PostMapping("/manager/user/delete/{id}")
    public String deleteSubmit(@ModelAttribute User user, @PathVariable UUID id, BindingResult bindingResult) {
		User current = userService.getUserByIdAndActive(id);
		if (current == null) {
			return "redirect:/error?code=404";
		}
		if (!current.getType().equals("external")) {
			logger.warn("GET request: Admin unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		userService.deleteUser(id);
		logger.info("POST request: Employee New modification request");
    	
        return "redirect:/manager/user";
    }
	
	@GetMapping("/manager/user/request/delete/{id}")
    public String deleteRequest(Model model, @PathVariable() UUID id) {
		ModificationRequest modificationRequest = userService.getModificationRequest(id);
		
		if (modificationRequest == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		
		// checks if manager is authorized for the request to approve
		if (!modificationRequest.getUserType().equals("external")) {
			logger.warn("GET request: Manager unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		
		model.addAttribute("modificationrequest", modificationRequest);
		logger.info("GET request: Manager external modification request by ID");
		
        return "manager/modificationrequest_delete";
    }
	
	@PostMapping("/manager/user/request/delete/{requestId}")
    public String deleteRequest(@PathVariable UUID requestId, @ModelAttribute ModificationRequest request, BindingResult bindingResult) {
		request = userService.getModificationRequest(requestId);
		
		// checks validity of request
		if (request == null) {
			return "redirect:/error?code=404&path=request-invalid";
		}
		
		// checks if manager is authorized for the request to approve
		if (!request.getUserType().equals("external")) {
			logger.warn("GET request: Manager unauthrorised request access");
			
			return "redirect:/error?code=401&path=request-unauthorised";
		}
		userService.deleteModificationRequest(request);
		logger.info("POST request: Manager approves modification request");
		
        return "redirect:/manager/user/request";

    }
	
	@GetMapping("/manager/employee/authorize")
    public String authorizeUser(@RequestParam(value="success", required=false) Boolean success, Model model) {
		ViewAuthorization authorization = new ViewAuthorization();
		authorization.setEmployee(new User());
		authorization.setExternal(new User());
		if (success != null && success == true) {
			model.addAttribute("success", true);
		}
		if (success != null && success == false) {
			model.addAttribute("success", false);
		}
		model.addAttribute("viewrequest", authorization);
		logger.info("GET request: Manager authorizes user");
		
        return "manager/requestaccess";
    }
	
	@PostMapping("/manager/employee/authorize")
    public String authorizeUser(@ModelAttribute("viewrequest") ViewAuthorization request, BindingResult bindingResult) {
		User external = userService.getUserByUsernameOrEmail(request.getExternal().getEmail());
		User employee = userService.getUserByUsernameOrEmail(request.getEmployee().getEmail());
		request.setEmployee(employee);
		request.setExternal(external);
		authorizeUserFormValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			return "manager/requestaccess";
		}
		viewAuthorizationService.createAuthorization(employee, external, true);
		logger.info("POST request: Manager authorizes user");
		
        return "redirect:/manager/employee/authorize?success=true";
    }
	
	@GetMapping("/manager/employee/request")
    public String getRequest(Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/error";
		}
		
		model.addAttribute("viewrequests", viewAuthorizationService.getPendingAuthorization());
		
        return "manager/accessrequests";
    }
	
	@GetMapping("/manager/employee/request/view/{id}")
    public String getRequest(@PathVariable UUID id, Model model) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/login";
		}
		
		ViewAuthorization authorization = viewAuthorizationService.getAuthorizationById(id);
		if (authorization == null) {
			return "redirect:/error?code=404";
		}
		model.addAttribute("viewrequest", authorization);
		
        return "manager/accessrequest_detail";
    }
	
	@PostMapping("/manager/employee/request/{id}")
    public String getRequests(@PathVariable UUID id, @ModelAttribute ViewAuthorization request, BindingResult bindingResult) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return "redirect:/login";
		}
		String status = request.getStatus();
		if (status == null || !(status.equals("approved") || status.equals("rejected"))) {
			return "redirect:/error?code=400";
		}
		
		ViewAuthorization authorization = viewAuthorizationService.getAuthorizationById(id);
		if (authorization == null) {
			return "redirect:/error?code=404";
		}
		authorization.setStatus(status);
		authorization = viewAuthorizationService.approveAuthorization(authorization);
		
        return "redirect:/manager/employee/request";
    }
}
