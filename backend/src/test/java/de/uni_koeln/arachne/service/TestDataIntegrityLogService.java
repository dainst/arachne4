package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestDataIntegrityLogService {

	private transient DataIntegrityLogService dataIntegrityLogService;
	
	private final String newLine = System.lineSeparator();
	
	@Before
	public void setUp() throws Exception {
		dataIntegrityLogService = new DataIntegrityLogService();
	}

	@Test
	public void testGetSummary() {
		assertEquals("Data Integrity Report" + newLine + "---------------------" + newLine 
				+ "No warnings.", dataIntegrityLogService.getSummary());
		logTestWarnings();
		assertEquals("Data Integrity Report" + newLine
				+ "---------------------" + newLine
				+ "'Test Warning #1.': 5" + newLine
				+ "'Test Warning #2.': 3" + newLine
				+ "'Test Warning #3.': 2" + newLine
				+ newLine
				+ "Total warnings: 10"
				, dataIntegrityLogService.getSummary());
	}
	
	@Test
	public void testClear() {
		logTestWarnings();
		dataIntegrityLogService.clear();
		assertEquals("Data Integrity Report" + newLine + "---------------------" + newLine 
				+ "No warnings.", dataIntegrityLogService.getSummary());
	}

	private void logTestWarnings() {
		dataIntegrityLogService.logWarning(1, "PS_TestID", "Test Warning #1.");
		dataIntegrityLogService.logWarning(2, "PS_TestID", "Test Warning #2.");
		dataIntegrityLogService.logWarning(3, "PS_TestID", "Test Warning #3.");
		dataIntegrityLogService.logWarning(4, "PS_TestID", "Test Warning #1.");
		dataIntegrityLogService.logWarning(5, "PS_TestID", "Test Warning #2.");
		dataIntegrityLogService.logWarning(6, "PS_TestID", "Test Warning #3.");
		dataIntegrityLogService.logWarning(7, "PS_TestID", "Test Warning #1.");
		dataIntegrityLogService.logWarning(8, "PS_TestID", "Test Warning #1.");
		dataIntegrityLogService.logWarning(9, "PS_TestID", "Test Warning #1.");
		dataIntegrityLogService.logWarning(10, "PS_TestID", "Test Warning #2.");
	}
}
