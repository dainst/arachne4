package de.uni_koeln.arachne.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import org.junit.Test;

public class TestDateUtils {

	@Test
	public void testDetermineDateFormat() {
		assertEquals("dd.MM.yy", DateUtils.determineDateFormat("02.01.85").toPattern());
		assertEquals("dd.MM.yyyy", DateUtils.determineDateFormat("02.01.1985").toPattern());
		assertEquals("MM/yyyy", DateUtils.determineDateFormat("11/2011").toPattern());
		assertEquals("yyyy", DateUtils.determineDateFormat("1996").toPattern());
		assertNull(DateUtils.determineDateFormat("asdfasdf"));
	}
	
	@Test
	public void testParseDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(1985, 0, 2, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("02.01.85"));
		assertEquals(cal.getTime(), DateUtils.parseDate("02.01.1985"));
		cal.set(2011, 10, 1, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("11/2011"));
		cal.set(1996, 0, 1, 0, 0);
		assertEquals(cal.getTime(), DateUtils.parseDate("1996"));
		assertNull(DateUtils.parseDate("asdf"));
	}

}
