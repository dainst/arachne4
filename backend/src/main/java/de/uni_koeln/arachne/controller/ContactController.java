package de.uni_koeln.arachne.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static de.uni_koeln.arachne.util.network.CustomMediaType.*;
import static de.uni_koeln.arachne.util.FormDataUtils.*;
import de.uni_koeln.arachne.service.MailService;
import de.uni_koeln.arachne.util.FormDataUtils.FormDataException;

/**
 * Controller that handles the HTTP API endpoint for the contact form. 
 * 
 * @author Reimar Grabowski
 */
@Controller
public class ContactController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);
	
	@Autowired
	private transient MailService mailService;
	
	private transient final String contactEmail;
	
	@Autowired
	public ContactController(final @Value("#{config.contactEmail}") String contactEmail) {
		this.contactEmail = contactEmail;
	}
	
	/**
	 * Implements the api endpoint for the contact form.
	 * 
	 * @param contactInformation The contact information, namely 'name', 'email', 'subject' and 'message'.
	 * @param response The outgoing HTTP response.
	 */
	@ResponseBody
	@RequestMapping(value="/contact"
			, method=RequestMethod.POST
			, consumes={APPLICATION_JSON_UTF8_VALUE})
	public void handleContactRequest(@RequestBody Map<String,String> contactInformation
			, final HttpServletResponse response) {
		
		checkForBot(contactInformation, "ui.contact.");
		
		final String name = getFormData(contactInformation, "name", true, "ui.contact.");
		final String eMailAddress = getFormData(contactInformation, "email", true, "ui.contact.");
		final String subject = getFormData(contactInformation, "subject", true, "ui.contact.");
		final String messageBody = getFormData(contactInformation, "message", true, "ui.contact.");
		
		final String replyTo = name + "<" + eMailAddress + ">";
				
		if (!mailService.sendMail(contactEmail, replyTo, "[Arachne4 via Kontaktformular] " + subject, messageBody)) {
			LOGGER.error("Unable to send contact form eMail.");
			response.setStatus(500);
			return;
		}
		response.setStatus(200);
		return;
	}
	
	/**
	 * Exception handler for FormDataExceptions.
	 * @param e The thrown <code>FormDataException</code>.
	 * @return A HTTP response with status 'bad request' and an error message as body.
	 */
	@ResponseBody
	@ExceptionHandler(FormDataException.class)
	public ResponseEntity<Map<String,String>> handleFromDataException(FormDataException e) {
		Map<String,String> body = new HashMap<String,String>();
		body.put("success", "false");
		body.put("message", e.getMessage());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", APPLICATION_JSON_UTF8_VALUE);
		return new ResponseEntity<Map<String,String>>(body, headers, HttpStatus.BAD_REQUEST);
	}
}
