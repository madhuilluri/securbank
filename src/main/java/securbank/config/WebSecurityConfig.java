package securbank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import securbank.authentication.CustomAuthenticationProvider;
import securbank.authentication.CustomAuthenticationSuccessHandler;

/**
 * @author Ayush Gupta
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	CustomAuthenticationSuccessHandler authSuccessHandler;
	
	@Autowired
	private CustomAuthenticationProvider customAuthenticationProvider;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {


//        http
//        .authorizeRequests()
//	        .antMatchers("/", "/home").permitAll()
//	        .anyRequest().authenticated()
//	        .and()
//        .formLogin()
//            .loginPage("/login")
//            .failureForwardUrl("/login?error")
//            .permitAll()
//            .and()
//        .logout()
//            .permitAll();

        http
        .authorizeRequests()
//        	.antMatchers("/admin/**").access("hasRole('ADMIN')")
	        .antMatchers("/", "/home").permitAll()
//	        .anyRequest().authenticated()
	        .and()
        .formLogin()
            .loginPage("/login")
            .successHandler(authSuccessHandler)
            //.failureForwardUrl("/login?error")
            .permitAll()
            
            .and()
        .logout()
            .permitAll();

    }

	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	auth.authenticationProvider(this.customAuthenticationProvider);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationTrustResolver trustResolver() {
        return new AuthenticationTrustResolverImpl();
    }
}
