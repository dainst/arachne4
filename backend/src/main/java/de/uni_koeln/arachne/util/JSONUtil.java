package de.uni_koeln.arachne.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JSONUtil {

	private transient final ObjectMapper objectMapper = new ObjectMapper();
	
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
