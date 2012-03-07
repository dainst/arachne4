package de.uni_koeln.arachne.sqlutil.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.sqlutil.SingleEntityQueryBuilder;
import de.uni_koeln.arachne.util.ArachneId;


public class TestArachneSingeEntityQueryBuilder {
	@Test
	public void testArachneSingeEntityQueryBuilder(){
		
		UserAdministration user = new UserAdministration();
		Set<DatasetGroup> set = new HashSet<DatasetGroup>();
		set.add(new DatasetGroup("Arachne"));
		user.setDatasetGroups(set);
		
		ArachneId id = new ArachneId("bauwerk", Long.valueOf(27000), Long.valueOf(100),false);
		SingleEntityQueryBuilder seqb = new SingleEntityQueryBuilder(id,user);
		assertEquals(seqb.getSQL(),"SELECT * FROM `bauwerk` WHERE 1 AND `bauwerk`.`PS_BauwerkID` = 27000 AND ( `bauwerk`.`DatensatzGruppeBauwerk` = \"Arachne\") Limit 1;");
		
		
	}
}
