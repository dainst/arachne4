package de.uni_koeln.arachne.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import org.junit.Test;

public class TestDateUtils {

	@Test
	public void testDetermineDateFormatddMMyy() {
		assertEquals("dd.MM.yy", DateUtils.determineDateFormat("02.01.85").toPattern());
	}
	
	@Test
	public void testDetermineDateFormatddMMyyyy() {
		assertEquals("dd.MM.yyyy", DateUtils.determineDateFormat("02.01.1985").toPattern());
	}
	
	@Test
	public void testDetermineDateFormatMMyyyy() {
		assertEquals("MM/yyyy", DateUtils.determineDateFormat("11/2011").toPattern());
	}
	
	@Test
	public void testDetermineDateFormatddyyyy() {
		assertEquals("yyyy", DateUtils.determineDateFormat("1996").toPattern());
	}
	
	@Test
	public void testDetermineDateFormatInvalid() {
		assertNull(DateUtils.determineDateFormat("asdfasdf"));
	}
	
	@Test
	public void testParseDateddMMyy() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(1985, 0, 2, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("02.01.85"));
	}
	
	@Test
	public void testParseDateddMMyyyy() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(1985, 0, 2, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("02.01.1985"));
	}
	
	@Test
	public void testParseDateMMyyyy() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(2011, 10, 1, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("11/2011"));
	}
	
	@Test
	public void testParseDateyyyy() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(1996, 0, 1, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("1996"));
	}
	
	@Test
	public void testParseDateInvalid() {
		assertNull(DateUtils.parseDate("asdf"));
	}

}
