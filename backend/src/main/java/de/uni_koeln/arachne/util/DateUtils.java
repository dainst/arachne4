package de.uni_koeln.arachne.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
	
	@SuppressWarnings("serial")
	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
	    put("^\\d{2}\\.\\d{2}\\.\\d{2}$", "dd.MM.yy");
	    put("^\\d{2}\\.\\d{2}\\.\\d{4}$", "dd.MM.yyyy");
	    put("^\\d{2}/\\d{4}$", "MM/yyyy");
	    put("^\\d{4}$", "yyyy");
	}};

	/**
	 * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
	 * format is unknown. You can simply extend DateUtil with more formats if needed.
	 * @param dateString The date string to determine the SimpleDateFormat pattern for.
	 * @return The matching SimpleDateFormat, or null if format is unknown.
	 * @see SimpleDateFormat
	 */
	public static SimpleDateFormat determineDateFormat(String dateString) {
	    for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
	        if (dateString.toLowerCase().matches(regexp)) {
	            return new SimpleDateFormat(DATE_FORMAT_REGEXPS.get(regexp));
	        }
	    }
	    return null; // Unknown format.
	}
	
	/**
	 * Parses a date string. First tries to determine format.
	 * Returns null if date can not be parsed with any of the
	 * known formats.
	 * @param dateString The date string to parse.
	 * @return A corresponding Date object, or null if parsing failed.
	 */
	public static Date parseDate(String dateString) {
		SimpleDateFormat format = determineDateFormat(dateString);
		if (format == null) return null;
		try {
			return format.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}

}
