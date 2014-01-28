package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service("MailService")
public class MailService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);
	
	private transient final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	
	public MailService() {
		mailSender.setHost("smtp.uni-koeln.de");
	}
	
	public boolean sendMail(final String recipient, final String subject, final String messageBody) {
		final SimpleMailMessage mailMessage = new SimpleMailMessage();
		// TODO make configurable
    	mailMessage.setFrom("arachne@uni-koeln.de");
    	// TODO validate eMail-Address
    	mailMessage.setTo(recipient);
    	mailMessage.setSubject(subject);
    	mailMessage.setText(messageBody);
    	
    	try {
    		mailSender.send(mailMessage);
    	} catch(MailException e) {
    		LOGGER.error("Sending email to '" + recipient + "' with subject '" + subject + "' failed with ", e);
    	}
		return false;
	}
}
