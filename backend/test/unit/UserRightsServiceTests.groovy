

import grails.test.*
import edu.yale.its.tp.cas.client.filter.CASFilter
import ArachneUsers
import javax.servlet.ServletContext
import org.springframework.mock.web.MockHttpSession
import javax.servlet.http.HttpSession

import org.springframework.web.context.request.RequestContextHolder

class UserRightsServiceTests extends GrailsUnitTestCase {
	def MockHttpSession mockSession;
	protected void setUp() {
        super.setUp()
		mockSession = new MockHttpSession();
    }
	
    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {
		//Creating a Mok Session
		
		mockSession.setAttribute(CASFilter.CAS_FILTER_USER,"Testman");
		//creating a Mok ArachneUsers Mok Object
		def temp = new ArachneUsers( groupID: 500,rightGroups:"Arachne,Oppenheim",username: 'Testman',institution: 'TheMighty',firstname: 'Test',lastname: 'Man',email: 'testman@testburg.com', street: '123 Fakestreet',
	   zip: '12345',
	   place: 'Testburg',
	   homepage: 'http://test.tst',
	   country: 'Testania',
	   telephone: '123456789',
	   all_groups: 'false',
	   login_permission: 'true',
	   lastLogin: new Date()) 
		def testInstances = [ temp ];
		mockDomain(ArachneUsers, testInstances);
		UserRightsService userRightsService = new UserRightsService();
		assertEquals userRightsService.getUsername(),'Testman' ;
		assertTrue userRightsService.isConfirmed();
		assertFalse userRightsService.isAuthorizedForAllGroups();
		
		assertEquals userRightsService.getDataGroups(),["Arachne","Oppenheim"];
		
		assertFalse userRightsService.isPermissionLevelValid(800);
		assertTrue userRightsService.isPermissionLevelValid(500);
		
		
    }
}
