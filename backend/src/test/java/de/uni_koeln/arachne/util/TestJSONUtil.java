package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.*;

public class TestJSONUtil {

	@Test
	public void testFixJSON() {
		// single value unescaped quotation marks
		String expected = "{\"test\":\"test \\\"1\\\"\"}";
		String actual = JSONUtil.fixJson("{\"test\":\"test \"1\"\"}");
		assertEquals(expected , actual);
		
		// single value unescaped quotation marks, missing trailing curly brace
		actual = JSONUtil.fixJson("{\"test\":\"test \"1\"\"");
		assertEquals(expected , actual);
		
		// multiple values unescaped backslash in on evalue
		expected = "{\"test1\":\"test 1\",\"test2\":\"test te\\\\st\"}";
		actual = JSONUtil.fixJson("{\"test1\":\"test 1\",\"test2\":\"test te\\st\"}");
		assertEquals(expected , actual);
		
		// multiple values unescaped backslash in on evalue, missing trailing curly brace
		actual = JSONUtil.fixJson("{\"test1\":\"test 1\",\"test2\":\"test te\\st\"");
		assertEquals(expected , actual);
		
		// multiple values only first can be parsed
		expected = "{\"test1\":\"test 1\"}";
		actual = JSONUtil.fixJson("{\"test1\":\"test 1\",\"test2\"");
		assertEquals(expected , actual);
		
		// single value unescaped ctrl-9 
		expected = "{\"test\":\"test ctrl-9\\\u0009\"}";
		actual = JSONUtil.fixJson("{\"test\":\"test ctrl-9\u0009\"");
		assertEquals(expected , actual);

		// single value unescaped ctrl-13 
		expected = "{\"test\":\"test ctrl-9\\\r\"}";
		actual = JSONUtil.fixJson("{\"test\":\"test ctrl-9\r\"");
		assertEquals(expected , actual);
	}


	// from https://stackoverflow.com/questions/2253750/compare-two-json-objects-in-java
	public static boolean areEqual(Object ob1, Object ob2) throws JSONException {
		Object obj1Converted = convertJsonElement(ob1);
		Object obj2Converted = convertJsonElement(ob2);
		return obj1Converted.equals(obj2Converted);
	}

	private static Object convertJsonElement(Object elem) throws JSONException {
		if (elem instanceof JSONObject) {
			JSONObject obj = (JSONObject) elem;
			Iterator<String> keys = obj.keys();
			Map<String, Object> jsonMap = new HashMap<>();
			while (keys.hasNext()) {
				String key = keys.next();
				jsonMap.put(key, convertJsonElement(obj.get(key)));
			}
			return jsonMap;
		} else if (elem instanceof JSONArray) {
			JSONArray arr = (JSONArray) elem;
			Set<Object> jsonSet = new HashSet<>();
			for (int i = 0; i < arr.length(); i++) {
				jsonSet.add(convertJsonElement(arr.get(i)));
			}
			return jsonSet;
		} else {
			return elem;
		}
	}
	
	/*@Test
	public void testGetObjectNode() {
		fail("Not yet implemented");
	}*/

}
