package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.sqlutil.SQLRightsConditionBuilder;


// TODO test may not work - check it
public class TestArachneSQLRightsConditionBuilder {
	
	@Test
	public void testArachneSQLRightsConditionBuilder(){
		
		final UserAdministration user = new UserAdministration();
		final Set<DatasetGroup> set = new HashSet<DatasetGroup>();
		set.add(new DatasetGroup("Arachne"));
		user.setDatasetGroups(set);
		
		
		SQLRightsConditionBuilder rcb = new SQLRightsConditionBuilder("bauwerk");
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), " AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\")");
		
		set.add(new DatasetGroup("Oppenheim"));
		
		rcb = new SQLRightsConditionBuilder("bauwerk");
		
		assertTrue(rcb.getUserRightsSQLSnipplett().contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\""));
		assertTrue(rcb.getUserRightsSQLSnipplett().contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Oppenheim\""));
		
		user.setAll_groups(true);
		
		rcb = new SQLRightsConditionBuilder("bauwerk");
		
		assertEquals(rcb.getUserRightsSQLSnipplett(), "");		
		
	}
	
}
