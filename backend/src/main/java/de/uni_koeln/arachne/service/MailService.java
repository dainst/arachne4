package de.uni_koeln.arachne.service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.util.StrUtils;

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
	 * Method to send a mail message. SMPT-Server and sender are configured servlet-wide via 'application.properties'.
	 * @param recipient The recipient of the message.
	 * @param subject The subject of the message.
	 * @param messageBody The body of the message.
	 * @return A flag indicating success of the operation.
	 */
	public boolean sendMail(final String recipient, final String subject, final String messageBody) {
		final SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		mailMessage.setFrom(sender);
		if (isValidEmailAddress(recipient)) {
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
		} else {
			LOGGER.error("Invalid recipient AddressException.");
			return false;
		}
	}
	
	private boolean isValidEmailAddress(final String address) {
	    
		if (address == null) {
	    	return false;
	    }
	    boolean result = true;
	    
	    try {
	    	// The variable is never used - the constructor is used to throw an AdressException in case of an invalid 
	    	// address
	    	@SuppressWarnings("unused")
	    	final InternetAddress inetAddress = new InternetAddress(address);
	    	if (!hasNameAndDomain(address)) {
	    		result = false;
	    	}
	    } catch (AddressException e) {
	      result = false;
	    }
	    return result;
	  }

	  private boolean hasNameAndDomain(final String address) {
	    final String[] tokens = address.split("@");
	    return tokens.length == 2 && stringHasContent(tokens[0]) && stringHasContent(tokens[1]);
	  }
	  
	  private boolean stringHasContent(final String string) {
		  // Although 'isEmptyOrNull' checks for null pointers the first check is needed as we use trim on the String
		  return string != null && StrUtils.isEmptyOrNull(string.trim());
	  }
}