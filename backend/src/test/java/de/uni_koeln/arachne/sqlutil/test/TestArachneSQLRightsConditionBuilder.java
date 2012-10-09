package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.sqlutil.SQLRightsConditionBuilder;
import de.uni_koeln.arachne.test.WebContextTestExecutionListener;


//TODO rewrite test to work with new SQLRightsConditionBuilder
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
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
		
		assertEquals(true, true);
	}
	
}
