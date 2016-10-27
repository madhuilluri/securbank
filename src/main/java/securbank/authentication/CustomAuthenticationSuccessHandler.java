package securbank.authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import securbank.services.AuthenticationService;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private AuthenticationService authService;
	
	@Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // Get the role of logged in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Cookie cookie = authService.validateCookie(request.getCookies(), (String)auth.getPrincipal());
        
        if (cookie == null) {
        	cookie = new Cookie("flag", "true");
        	
        }
        cookie.setMaxAge(30*24*60*60 );
        response.addCookie(cookie);
        
        String role = auth.getAuthorities().toString();
        
        if(role==null){
        	return "/";
        }

        return authService.getRedirectUrlFromRole(role);
    }

}
