package de.uni_koeln.arachne.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Controller that handles the HTTP API endpoint for the contact form. 
 * 
 * @author Reimar Grabowski
 */
@Controller
public class ContactController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);
	
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
			, consumes="application/json;charset=UTF-8"
			, produces="application/json;charset=UTF-8")
	public void changePasswordAfterResetRequest(@RequestBody Map<String,String> contactInformation
			, final HttpServletResponse response) {
		System.out.println(contactEmail);
		final String name = StrUtils.getFormData(contactInformation, "name", true, "ui.contact.");
		final String eMailAddress = StrUtils.getFormData(contactInformation, "email", true, "ui.contact.");
		final String subject = StrUtils.getFormData(contactInformation, "subject", true, "ui.contact.");
		final String message = StrUtils.getFormData(contactInformation, "message", true, "ui.contact.");
		
		final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.uni-koeln.de");

		final SimpleMailMessage userMail = new SimpleMailMessage();
		userMail.setFrom("arachne@uni-koeln.de");
		userMail.setTo(contactEmail);
		userMail.setReplyTo(name + "<" + eMailAddress + ">");
		userMail.setSubject("[Arachne4 via Kontaktformular] " + subject);
		userMail.setText(message);
		
		try {
			mailSender.send(userMail);
		} catch(MailException e) {
			LOGGER.error("Unable to send contact form eMail. ", e);
			response.setStatus(500);
			return;
		}
		response.setStatus(200);
		return;
	}
}
