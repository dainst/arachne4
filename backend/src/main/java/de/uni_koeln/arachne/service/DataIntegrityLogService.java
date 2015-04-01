package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service that gathers information about problems with the data integrity of the DB (mostly during data imports).
 * 
 * @author Reimar Grabowski
 *
 */
@Service
public class DataIntegrityLogService {
	private static final Logger LOGGER = LoggerFactory.getLogger("DataIntegrityLogger");
	
	private transient final List<DataIntegrityWarning> warnings = new ArrayList<DataIntegrityWarning>();
	
	/**
	 * Adds a warning to the list of warnings this class manages.
	 * @param identifier The identifier of the object that caused the warning (normally a DB key).
	 * @param identifierType The type of the identifier (normally a DB key name like 'PSObjekt_ID').
	 * @param message 
	 */
	public void logWarning(final long identifier, final String identifierType, final String message) {
		warnings.add(new DataIntegrityWarning(identifier, identifierType, message));
		LOGGER.warn(message + " " + identifierType + ": " + identifier);
	}
	
	/**
	 * Returns a summary of the currently logged warnings.
	 * @return A string list containing the summary.
	 */
	public String getSummary() {
		// magic number is length of "No warnings."
		final StringBuilder result = new StringBuilder(12); 
		final Set<String> uniqueWarnings = getUniqueWarnings();
		int totalCount = 0;
		
		for (final String message : uniqueWarnings) {
			final int count = getWarningsByMessage(message).size();
			result.append("'" + message + "': " + count + "\n");
			totalCount += count;
		}
		
		if (result.toString().isEmpty()) {
			result.append("No warnings.");
		} else {
			result.append("\n");
			result.append("Total warnings: " + totalCount);
		}
		
		return result.toString();
	}
	
	/**
	 * Clears the warnings lost.
	 */
	public void clear() {
		warnings.clear();
	}
	
	/**
	 * Returns the sorted unique warning messages logged.	
	 * @return A set of unique warning messages.
	 */
	private Set<String> getUniqueWarnings() {
		final Set<String> result = new TreeSet<String>();
		for (final DataIntegrityWarning warning: warnings) {
			result.add(warning.getMessage());
		}
		return result;
	}
	
	/**
	 * Returns all warnings with the given message.
	 * @param message The message that all warnings share.
	 * @return A set of warnings.
	 */
	private Set<DataIntegrityWarning> getWarningsByMessage(final String message) {
		final Set<DataIntegrityWarning> result = new HashSet<DataIntegrityWarning>();
		for (final DataIntegrityWarning warning: warnings) {
			if (warning.getMessage().equals(message)) {
				result.add(warning);
			}
		}
		return result;
	}
}
