package de.uni_koeln.arachne.util.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic networking utility methods.
 * 
 *  @author Reimar Grabowski
 */
public class BasicNetwork {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicNetwork.class);

	/**
	 * Determines the host name as <code>String</code>.
	 * @return The host name of the system or "UnknownHost" in case of failure.
	 */
	public static String getHostName() {
		String result = "UnknownHost";
		try {
			final InetAddress localHost = InetAddress.getLocalHost();
			result = localHost.getHostName();
		} catch (UnknownHostException e) {
			LOGGER.warn("Could not determine local host name.");
		}
		return result;
	}
}
