package securbank.authentication;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import securbank.models.User;
import securbank.services.AuthenticationService;

/**
 * @author Ayush Gupta
 *
 */

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
	@Autowired
	private AuthenticationService auth;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		User user = auth.verifyUser(authentication.getPrincipal().toString(), authentication.getCredentials().toString());
		
		if (user == null) {
			throw new BadCredentialsException("Invalid Username or Password.");
		}
		
		auth.updateLoginTime(user);
		
		
		List<GrantedAuthority> roles = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole());
		
		return new UsernamePasswordAuthenticationToken(user.getUsername(), authentication.getCredentials().toString(), roles);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}
}
