package de.uni_koeln.arachne.sqlutil;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSQLToolbox {
	@Test
	public void testGeneratePrimaryKeyName() {
		assertEquals(SQLToolbox.generatePrimaryKeyName("test"), "PS_TestID");
		assertEquals(SQLToolbox.generatePrimaryKeyName("marbilder"), "PS_MARBilderID");
		assertEquals(SQLToolbox.generatePrimaryKeyName("zenon"), "zenonid");
	}
	
	@Test
	public void testGenerateForeignKeyName() {
		assertEquals(SQLToolbox.generateForeignKeyName("test"), "FS_TestID");
		assertEquals(SQLToolbox.generateForeignKeyName("marbilder"), "FS_MARBilderID");
	}
	
	@Test
	public void testGenerateDatasetGroupName() {
		assertEquals(SQLToolbox.generateDatasetGroupName("test"), "DatensatzGruppeTest");
		assertEquals(SQLToolbox.generateDatasetGroupName("marbilder"), "DatensatzGruppeMARBilder");
	}
		
	@Test
	public void testAddBackticks() {
		assertEquals(SQLToolbox.addBackticks("test"), "`test`");
	}
		
	@Test
	public void testGetQualifiedFieldName() {
		assertEquals(SQLToolbox.getQualifiedFieldname("test", "kurzbeschreibungTest")
				, "`test`.`kurzbeschreibungTest`");
	}
	
	@Test
	public void testUCFirst() {
		assertEquals(SQLToolbox.ucFirst("test"), "Test" );
	}
}