package securbank.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityContextService")
public class SecurityContextServiceImpl implements SecurityContextService {
	@Autowired
	  private AuthenticationTrustResolver authenticationTrustResolver;

	  @Override
	  public boolean isCurrentAuthenticationAnonymous() {
	    final Authentication authentication =
	        SecurityContextHolder.getContext().getAuthentication();
	    return authenticationTrustResolver.isAnonymous(authentication);
	  }

}
