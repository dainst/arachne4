/**
 * 
 */
package de.uni_koeln.arachne.util;

import java.util.Map;

/**
 * Utility class for form data handling.
 * 
 * @author Reimar Grabowski
 */
public class FormDataUtils {
	/**
	 * Form data exception class.
	 */
	@SuppressWarnings("serial")
	public static class FormDataException extends RuntimeException {
		public FormDataException(String message) {
			super(message);
		}
	}
	
	/**
	 * Simple attempt to keep bots from issuing requests.
	 * 
	 * @param formData The form data of the request.
	 */
	public static void checkForBot(Map<String, String> formData, final String messagePrefix) throws FormDataException {
		if (!(formData.containsKey("iAmHuman") && formData.get("iAmHuman").equals("humanIAm"))) {
			throw new FormDataException(messagePrefix + "bot");
		}
	}
	
	/**
	 * Utility method to retrieve one field.
	 * @param formData The data to retrieve the field from.
	 * @param fieldName The name of the field to retrieve.
	 * @param required Flag indicating if the field is required.
	 * @param messagePrefix A message prefix for any thrown <code>FormDataExceptions</code>
	 * @return The field value.
	 * @throws FormDataException If a required field is not found.
	 */
	public static String getFormData(final Map<String, String> formData, final String fieldName, final boolean required
			, final String messagePrefix) throws FormDataException {
		if (required && (!formData.containsKey(fieldName) || formData.get(fieldName).isEmpty())) {
			throw new FormDataException(messagePrefix + "fieldMissing." + fieldName);
		} else {
			return formData.get(fieldName);
		}
	}
}
