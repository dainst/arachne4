package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/**
 * Simple service to send eMail messages.
 */
@Service("MailService")
public class MailService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);
	
	private transient final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	
	private transient final String sender;
	
	/**
	 * Constructor initializing the SMTP host and the mail sender. 
	 */
	@Autowired
	public MailService(final @Value("#{config.mailSMTPServer}") String smtpServer
			, final @Value("#{config.mailSender}") String sender) {
		
		mailSender.setHost(smtpServer);
		this.sender = sender;
	}
	
	/**
	 * Method the send a mail message.
	 * @param recipient The recipient of the message.
	 * @param subject The subject of the message.
	 * @param messageBody The body of the message.
	 * @return A flag indicating success of the operation.
	 */
	public boolean sendMail(final String recipient, final String subject, final String messageBody) {
		final SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		mailMessage.setFrom(sender);
    	// TODO validate eMail-Address
    	mailMessage.setTo(recipient);
    	mailMessage.setSubject(subject);
    	mailMessage.setText(messageBody);
    	
    	try {
    		mailSender.send(mailMessage);
    		LOGGER.debug("Sending email to '" + recipient + "' with subject '" + subject + "' suceeded");
    	} catch(MailException e) {
    		LOGGER.error("Sending email to '" + recipient + "' with subject '" + subject + "' failed with ", e);
    		return false;
    	}
		return true;
	}
}