package de.uni_koeln.arachne.util.sql;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koeln.arachne.util.sql.SQLToolbox;

public class TestSQLToolbox {
	@Test
	public void testGeneratePrimaryKeyName() {
		// test build in exceptions
		assertEquals("PS_MARBilderID", SQLToolbox.generatePrimaryKeyName("marbilder"));
		assertEquals("zenonid", SQLToolbox.generatePrimaryKeyName("zenon"));
		assertEquals("ArachneEntityID", SQLToolbox.generatePrimaryKeyName("arachneentitydegrees"));
		// test standard
		assertEquals("PS_TestID", SQLToolbox.generatePrimaryKeyName("test"));
		// Test standard (from cache)
		assertEquals("PS_TestID", SQLToolbox.generatePrimaryKeyName("test"));
	}
	
	@Test
	public void testGenerateForeignKeyName() {
		assertEquals("FS_TestID", SQLToolbox.generateForeignKeyName("test"));
		assertEquals("FS_MARBilderID", SQLToolbox.generateForeignKeyName("marbilder"));
	}
	
	@Test
	public void testGenerateDatasetGroupName() {
		assertEquals("DatensatzGruppeTest", SQLToolbox.generateDatasetGroupName("test"));
		assertEquals("DatensatzGruppeMARBilder", SQLToolbox.generateDatasetGroupName("marbilder"));
	}
		
	@Test
	public void testAddBackticks() {
		assertEquals("`test`", SQLToolbox.addBackticks("test"));
	}
		
	@Test
	public void testGetQualifiedFieldName() {
		assertEquals("`test`.`kurzbeschreibungTest`"
				,SQLToolbox.getQualifiedFieldname("test", "kurzbeschreibungTest"));
	}
	
	@Test
	public void testUCFirst() {
		assertEquals("Test", SQLToolbox.ucFirst("test"));
	}
}