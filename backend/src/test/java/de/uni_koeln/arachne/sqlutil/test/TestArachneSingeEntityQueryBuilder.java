package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_koeln.arachne.sqlutil.SingleEntityQueryBuilder;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.UserRightsSingleton;


public class TestArachneSingeEntityQueryBuilder {
	@Test
	public void testArachneSingeEntityQueryBuilder(){
		
List<String> groups = new ArrayList<String>(1);
		
		groups.add("Arachne");
		
		ArachneId id = new ArachneId("bauwerk", Long.valueOf(27000), Long.valueOf(100),false);
		UserRightsSingleton.init("Testman", false, true, 500, groups);
		SingleEntityQueryBuilder seqb = new SingleEntityQueryBuilder(id);
		assertEquals(seqb.getSQL(),"SELECT * FROM `bauwerk` WHERE 1 AND `bauwerk`.`PS_BauwerkID` = 27000 AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\") Limit 1;");
		
		
	}
}
