package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.sqlutil.SQLRightsConditionBuilder;



public class TestArachneSQLRightsConditionBuilder {
	
	@Test
	public void testArachneSQLRightsConditionBuilder(){
		
		UserAdministration user = new UserAdministration();
		Set<DatasetGroup> set = new HashSet<DatasetGroup>();
		set.add(new DatasetGroup("Arachne"));
		user.setDatasetGroups(set);
		
		
		SQLRightsConditionBuilder rcb = new SQLRightsConditionBuilder("bauwerk", user);
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), " AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\")");
		
		set.add(new DatasetGroup("Oppenheim"));
		
		rcb = new SQLRightsConditionBuilder("bauwerk", user);
		
		assertTrue(rcb.getUserRightsSQLSnipplett().contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\""));
		assertTrue(rcb.getUserRightsSQLSnipplett().contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Oppenheim\""));
		
		user.setAll_groups(true);
		
		rcb = new SQLRightsConditionBuilder("bauwerk", user);
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), "");		
		
	}
	
}
