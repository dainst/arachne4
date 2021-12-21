package de.uni_koeln.arachne.util.security;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * Class for constructing secure random entities. Since constructing a SecureRandom object is expensive it is kept 
 * around here and a getter is provided.
 * @author Reimar Grabowski
 */
@Component
public class Random {
	private final SecureRandom secureRandom = new SecureRandom();
	
	/**
	 * Generates a secure token with 130 random bits. Therefore a secure random generator is used and the resulting value 
	 * is base 32 encoded.
	 * @return An alphanumeric random token.
	 */
	public String getNewToken() {
		return new BigInteger(130, secureRandom).toString(32);
	}
	
	/**
	 * Getter for the SecureRandom of this class.
	 * @return The secure random number generator.
	 */
	public SecureRandom getSecureRandom() {
		return secureRandom;
	}
}
