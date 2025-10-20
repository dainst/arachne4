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
 * 
 * @author Reimar Grabowski
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private transient ArachneUserDetailsService arachneUserDetailsService;

	/*
	 * ~~(Migrate manually based on
	 * https://spring.io/blog/2022/02/21/spring-security-without-the-
	 * websecurityconfigureradapter)~~>
	 */@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(arachneUserDetailsService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.authorizeRequests(requests -> requests
						.requestMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("USER", "ADMIN", "ANONYMOUS")
						.requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/userinfo/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/userinfo/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/user/**").hasAnyRole("ANONYMOUS", "USER", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/search/scroll/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/entity/**/images").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/**").hasAnyRole("USER", "ADMIN", "ANONYMOUS"))
				.httpBasic(basic -> basic
						.authenticationEntryPoint(authenticationEntryPoint()))
				.sessionManagement(management -> management
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
	}

	/**
	 * Returns a new {@link ArachneAuthenticationEntryPoint}.
	 * 
	 * @return The entry point.
	 */
	@Bean
	AuthenticationEntryPoint authenticationEntryPoint() {
		return new ArachneAuthenticationEntryPoint();
	}
}
