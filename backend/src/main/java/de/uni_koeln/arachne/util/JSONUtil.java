package de.uni_koeln.arachne.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONUtil {

	private static final String JSON_START = "{\"";
	private static final String JSON_END = "\"}";
	private static final String JSON_SEPARATOR = "\",\"";
	private static final String JSON_KV_SEPARATOR = "\":\"";
	private static Pattern escape = Pattern.compile("([\"\\\\[\u0000-\u001f]])");
	
	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	public static ObjectNode getObjectNode() {
		return MAPPER.getNodeFactory().objectNode();
	}

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
				System.out.println(keyValue);
				int colon = keyValue.indexOf(JSON_KV_SEPARATOR);
				if (colon > 0) {
					String key = keyValue.substring(0, colon + 2);
					System.out.println("K: " + key);
					String value = keyValue.substring(colon + 3, keyValue.length());
					Matcher matcher = escape.matcher(value);
					value = matcher.replaceAll("\\\\$1");
					System.out.println("V: " + value);
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
		System.out.println(result.toString());
		System.out.println("---");
		return result.toString();
	}
}
