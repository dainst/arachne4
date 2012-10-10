package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.uni_koeln.arachne.sqlutil.SQLRightsConditionBuilder;
import de.uni_koeln.arachne.test.WebContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestArachneSQLRightsConditionBuilder {
	
	@Test
	public void testArachneSQLRightsConditionBuilder(){
		SQLRightsConditionBuilder rcb = new SQLRightsConditionBuilder("bauwerk");
		
		assertTrue(rcb.getUserRightsSQLSnipplett().contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\""));
		assertTrue(rcb.getUserRightsSQLSnipplett().contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Oppenheim\""));
	}	
}
