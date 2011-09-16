package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_koeln.arachne.sqlutil.ArachneSingleEntityQueryBuilder;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.ArachneUserRightsSingleton;


public class TestArachneSingeEntityQueryBuilder {
	@Test
	public void testArachneSingeEntityQueryBuilder(){
		
List<String> groups = new ArrayList<String>(1);
		
		groups.add("Arachne");
		
		ArachneId id = new ArachneId("bauwerk",new Long(27000),new Long(100),false);
		ArachneUserRightsSingleton.init("Testman", false, true, 500, groups);
		ArachneSingleEntityQueryBuilder seqb = new ArachneSingleEntityQueryBuilder(id);
		assertEquals(seqb.getSQL(),"SELECT * FROM `bauwerk` WHERE 1 AND `bauwerk`.`PS_BauwerkID` = 27000 AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\") Limit 1;");
		
		
	}
}
