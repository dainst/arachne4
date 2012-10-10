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

import de.uni_koeln.arachne.sqlutil.SingleEntityQueryBuilder;
import de.uni_koeln.arachne.test.WebContextTestExecutionListener;
import de.uni_koeln.arachne.util.EntityId;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestArachneSingeEntityQueryBuilder {
	@Test
	public void testArachneSingeEntityQueryBuilder(){
		final EntityId entityId = new EntityId("bauwerk", Long.valueOf(27000), Long.valueOf(100),false);
		final SingleEntityQueryBuilder queryBuilder = new SingleEntityQueryBuilder(entityId);
		
		// do not test for equality as "Oppenheim" and "Arachne" may be switched
		final String sqlQuery = queryBuilder.getSQL();
		assertTrue(sqlQuery.startsWith("SELECT * FROM `bauwerk` WHERE 1 AND `bauwerk`.`PS_BauwerkID` = 27000 AND"));
		assertTrue(sqlQuery.contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Oppenheim\""));
		assertTrue(sqlQuery.contains(" OR "));
		assertTrue(sqlQuery.contains("`bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\""));
		assertTrue(sqlQuery.endsWith("Limit 1;"));
	}
}
