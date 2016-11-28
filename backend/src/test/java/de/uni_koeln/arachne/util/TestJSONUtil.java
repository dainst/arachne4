package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

import org.junit.Test;

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
	
	/*@Test
	public void testGetObjectNode() {
		fail("Not yet implemented");
	}*/

}
