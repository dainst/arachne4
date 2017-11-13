package de.uni_koeln.arachne.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
/**
 * Class that provides an object mapper instance and JSON related constants and functions.
 * @author Reimar Grabowski
 *
 */
public class JSONUtil {

	private static final String JSON_START = "{\"";
	private static final String JSON_END = "\"}";
	private static final String JSON_SEPARATOR = "\",\"";
	private static final String JSON_KV_SEPARATOR = "\":\"";
	private static Pattern escape = Pattern.compile("([\"\\\\[\u0000-\u001f]])");
	
	/**
	 * Jackson object mapper. Use this one instead of creating a new one.
	 */
	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * Gets a Jackson object node from the node factory.
	 * @return The new object node.
	 */
	public static ObjectNode getObjectNode() {
		return MAPPER.getNodeFactory().objectNode();
	}

	/**
	 * Function to 'repair' invalid JSON. It tries to fix unparsable JSON by escaping special chars in string values, adding 
	 * missing curly braces and trying to at least get all the values before the value that caused the parse exception. It only 
	 * meant for simple JSON of the form {"key1":"value1","key2":"value2",etc.}.  
	 * @param invalidJSON The unparsable JSON.
	 * @return The 'repaired' JSON.
	 */
	public static String fixJson(final String invalidJSON) {
		StringBuilder result = new StringBuilder("{");
		if (invalidJSON.startsWith(JSON_START)) {
			final List<Integer> separators = new ArrayList<>();
			separators.add(0);
			int index = invalidJSON.indexOf(JSON_SEPARATOR);
			while (index >= 0) {
				separators.add(index);
				index = invalidJSON.indexOf(JSON_SEPARATOR, index + 1);
			}
			int end = invalidJSON.indexOf(JSON_END);
			end = (end >= 0) ? end : invalidJSON.lastIndexOf("\"");
			separators.add(end);
			for (int i=0; i<separators.size() - 1; i++) {
				int beginIndex = separators.get(i);
				int endIndex = separators.get(i+1);
				String keyValue = invalidJSON.substring(beginIndex + 1, endIndex);
				int colon = keyValue.indexOf(JSON_KV_SEPARATOR);
				if (colon > 0) {
					String key = keyValue.substring(0, colon + 2);
					String value = keyValue.substring(colon + 3, keyValue.length());
					Matcher matcher = escape.matcher(value);
					value = matcher.replaceAll("\\$1");
					result.append(key);
					result.append("\"");
					result.append(value);
					result.append("\"");
				} else {
					break; 
				}
			}
			result.append("}");
		}
		return result.toString();
	}
}
