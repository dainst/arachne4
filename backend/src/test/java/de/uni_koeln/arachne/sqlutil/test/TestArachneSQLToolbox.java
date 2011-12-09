package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_koeln.arachne.sqlutil.SQLToolbox;


public class TestArachneSQLToolbox {
	@Test
	public void testArachneSQLToolbox(){
		assertEquals(SQLToolbox.addBackticks("test"),"`test`" );
		assertEquals(SQLToolbox.generateForeignKeyName("bauwerk"),"FS_BauwerkID" );
		assertEquals(SQLToolbox.generatePrimaryKeyName("bauwerk"),"PS_BauwerkID" );
		assertEquals(SQLToolbox.getQualifiedFieldname("bauwerk", "kurzbeschreibungBauwerk"),"`bauwerk`.`kurzbeschreibungBauwerk`" );
		assertEquals(SQLToolbox.ucfirst("bauwerk"),"Bauwerk" );
		
	}
}
