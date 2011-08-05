package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_koeln.arachne.sqlutil.ArachneSQLToolbox;


public class TestArachneSQLToolbox {
	@Test
	public void testArachneSQLToolbox(){
		assertEquals(ArachneSQLToolbox.addBackticks("test"),"`test`" );
		assertEquals(ArachneSQLToolbox.generateForeignKeyName("bauwerk"),"FS_BauwerkID" );
		assertEquals(ArachneSQLToolbox.generatePrimaryKeyName("bauwerk"),"PS_BauwerkID" );
		assertEquals(ArachneSQLToolbox.getQualifiedFieldname("bauwerk", "kurzbeschreibungBauwerk"),"`bauwerk`.`kurzbeschreibungBauwerk`" );
		assertEquals(ArachneSQLToolbox.ucfirst("bauwerk"),"Bauwerk" );
		
	}
}
