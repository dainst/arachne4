package de.uni_koeln.arachne.util.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom AuthenticationEntryPoint implementation to suppress the browsers login dialog on validation failure.
 * @author Reimar Grabowski
 */
@Component
public class ArachneAuthenticationEntryPoint implements	AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		
		// WWW-Authenticate header should be set as FormBased , else browser will show login dialog with realm
        response.setHeader("WWW-Authenticate", "FormBased");
        response.setStatus( HttpServletResponse.SC_UNAUTHORIZED);
	}

}
