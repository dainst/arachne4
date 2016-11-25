package de.uni_koeln.arachne.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONUtil {

	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	public static ObjectNode getObjectNode() {
		return MAPPER.getNodeFactory().objectNode();
	}
}
