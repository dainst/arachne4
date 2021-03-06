package de.uni_koeln.arachne.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;

import de.uni_koeln.arachne.service.ArachneUserDetailsService;
import de.uni_koeln.arachne.util.security.ArachneAuthenticationEntryPoint;

/**
 * Spring security configuration class.
 * @author Reimar Grabowski
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private transient ArachneUserDetailsService arachneUserDetailsService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(arachneUserDetailsService);
	}	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("USER", "ADMIN", "ANONYMOUS")
				.antMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
				.antMatchers(HttpMethod.GET, "/userinfo/**").hasAnyRole("USER", "ADMIN")
				.antMatchers(HttpMethod.PUT, "/userinfo/**").hasAnyRole("USER", "ADMIN")
				.antMatchers(HttpMethod.POST, "/user/**").hasAnyRole("ANONYMOUS", "USER", "ADMIN")
				.antMatchers(HttpMethod.GET, "/search/scroll/**").hasAnyRole("USER", "ADMIN")
				.antMatchers(HttpMethod.GET, "/entity/**/images").hasAnyRole("USER", "ADMIN")
				.antMatchers("/**").hasAnyRole("USER", "ADMIN", "ANONYMOUS")
			.and()
				.httpBasic()
				.authenticationEntryPoint(authenticationEntryPoint())
			.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	/**
	 * Returns a new {@link ArachneAuthenticationEntryPoint}.
	 * @return The entry point.
	 */
	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return new ArachneAuthenticationEntryPoint();
	}
}
