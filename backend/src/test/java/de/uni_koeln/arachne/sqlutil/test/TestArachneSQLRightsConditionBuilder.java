package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_koeln.arachne.sqlutil.ArachneSQLRightsConditionBuilder;
import de.uni_koeln.arachne.util.ArachneUserRightsSingleton;



public class TestArachneSQLRightsConditionBuilder {
	@Test
	public void testArachneSQLRightsConditionBuilder(){
		List<String> groups = new ArrayList<String>(1);
		
		groups.add("Arachne");
		
		
		ArachneUserRightsSingleton.init("Testman", false, true, 500, groups);
		
		ArachneSQLRightsConditionBuilder rcb = new ArachneSQLRightsConditionBuilder("bauwerk");
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), " AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\")");
		
		groups = new ArrayList<String>(2);
		
		groups.add("Arachne");
		groups.add("Oppenheim");
		
		ArachneUserRightsSingleton.init("Testman", false, true, 500, groups);
		
		rcb = new ArachneSQLRightsConditionBuilder("bauwerk");
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), " AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\" OR `bauwerk`.`DatensatzGruppeBauwerk` = \"Oppenheim\")");
		
		
		groups = new ArrayList<String>(2);
		
		groups.add("Arachne");
		groups.add("Oppenheim");
		
		ArachneUserRightsSingleton.init("Testman", true, true, 900, groups);
		
		rcb = new ArachneSQLRightsConditionBuilder("bauwerk");
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), "");
		
		
	}
}
