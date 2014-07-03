package de.uni_koeln.arachne.sqlutil;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.EntityId;

@RunWith(MockitoJUnitRunner.class) 
public class TestSQLFactory {
	
	@Mock private IUserRightsService mockUserRightsService;
	@InjectMocks private SQLFactory sqlFactory = new SQLFactory();
	
	@Before
	public void setUp() {
		final UserAdministration user = new UserAdministration();
		// set mock user name to 'INDEXING' to not trigger the SQL user rights snippet creation  
		user.setUsername(IUserRightsService.INDEXING);
		Mockito.when(mockUserRightsService.getCurrentUser()).thenReturn(user);
		// set custom SQL snippet
		Mockito.when(mockUserRightsService.getSQL(Mockito.anyString())).thenReturn("insertPermissionSQLhere");
	}
	
	@Test
	public void testGetSingleEntityQuery() {
		final EntityId entityId = new EntityId("test", Long.valueOf(27000), Long.valueOf(100),false);
			
		final String sqlQuery = sqlFactory.getSingleEntityQuery(entityId);
		
		assertTrue(sqlQuery.startsWith("SELECT * FROM `test` WHERE `test`.`PS_TestID` = 27000"));
		assertTrue(sqlQuery.contains("insertPermissionSQLhere"));
		assertTrue(sqlQuery.endsWith("LIMIT 1;"));
	}
	
	@Test
	public void testGetIntFieldByIdQuery() {
		final String sqlQuery = sqlFactory.getIntFieldByIdQuery("test", 1, "testfield");
		
		assertTrue(sqlQuery.startsWith("SELECT `test`.`testfield` FROM `test` WHERE `test`.`PS_TestID` = \"1\" AND `test`.`testfield` IS NOT NULL"));
		assertTrue(sqlQuery.contains("insertPermissionSQLhere"));
		assertTrue(sqlQuery.endsWith(";"));
	}
}
