package de.uni_koeln.arachne.util;

import java.util.List;

/**
 * Class implementing string utility functions.
 */
public class StrUtils {	
	/**
	 * This method returns if a <code>String</code> is empty or <code>null</code> or has the value "0".
	 * @param string to test.
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(String string) {
		boolean result = string == null || string.isEmpty();
		if (result) {
			return result;
		} else {
			return "0".equals(string);
		}
	}
	
	/**
	 * This method returns if a <code>List<String></code> is empty or <code>null</code>.
	 * @param stringList to test.
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(final List<String> stringList) {
		return stringList == null || stringList.isEmpty();
	}

	/**
	 * This method returns if a <code>StringBuilder</code> is empty or <code>null</code>.
	 * @param stringBuilder
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(final StringBuilder stringBuilder) {
		return stringBuilder == null || stringBuilder.length() < 1;
	}
	
	/**
	 * Function to check if a given string is a valid IP address.
	 * @param ipAddress The string to check for validity
	 * @return A boolean value indicating if the given string is a valid IP address.
	 */
	public static boolean isValidIPAddress(final String ipAddress)
	{
	    final String[] parts = ipAddress.split( "\\." );

	    if (parts.length != 4) {
	        return false;
	    }
	    
	    try {
	    	for (final String string : parts) {
	    		final int index = Integer.parseInt(string);
	    		if ((index < 0) || (index > 255)) {
	    			return false;
	    		}
	    	}
	    } catch (NumberFormatException e) {
	    	return false;
	    }
	    
	    return true;
	}
	
	public static String urlEncodeQuotationMarks(final String inputString) {
		return inputString.replaceAll("\"", "%22");
	}
}
