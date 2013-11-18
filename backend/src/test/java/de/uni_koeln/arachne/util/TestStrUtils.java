package de.uni_koeln.arachne.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestStrUtils {
	@Test
	public void testIfEmptyOrNullString() {
		String testString = null;
		assertTrue(StrUtils.isEmptyOrNullOrZero(testString));
		
		testString = "";
		assertTrue(StrUtils.isEmptyOrNullOrZero(testString));
		
		testString = "test";
		assertFalse(StrUtils.isEmptyOrNullOrZero(testString));
	}
	
	@Test
	public void testIfEmptyOrNullStringList() {
		List<String> testList = null;
		assertTrue(StrUtils.isEmptyOrNull(testList));
		
		testList = new ArrayList<String>();
		assertTrue(StrUtils.isEmptyOrNull(testList));
		
		testList.add("Test");
		assertFalse(StrUtils.isEmptyOrNull(testList));
	}
	
	@Test
	public void testIfEmptyOrNullStringBuilder() {
		StringBuilder testBuilder = null;
		assertTrue(StrUtils.isEmptyOrNull(testBuilder));
		
		testBuilder = new StringBuilder();
		assertTrue(StrUtils.isEmptyOrNull(testBuilder));
		
		testBuilder.append("Test");
		assertFalse(StrUtils.isEmptyOrNull(testBuilder));
	}
	
	@Test
	public void testIsValidIPAddress() {
		String testIP = "127.0.0.1"; // NOPMD
		assertTrue(StrUtils.isValidIPAddress(testIP));
		
		testIP = "255.255.255.255"; // NOPMD	
		assertTrue(StrUtils.isValidIPAddress(testIP));
		
		testIP = "127.0.0.256";
		assertFalse(StrUtils.isValidIPAddress(testIP));
		
		testIP = "www.uni-koeln.de";
		assertFalse(StrUtils.isValidIPAddress(testIP));
		
		testIP = "this.is.no.ipaddress";
		assertFalse(StrUtils.isValidIPAddress(testIP));
	}
}
