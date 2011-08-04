package de.uni_koeln.arachne.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.ArachneDatasetMapping;
import de.uni_koeln.arachne.responseobjects.ArachneDataset;
import de.uni_koeln.arachne.util.ArachneId;

@Repository("arachneSingleDatasetDao")
public class ArachneSingleDatasetDao extends SQLDao {

	public ArachneDataset getById(ArachneId id){
		//TODO Nicer SQL Building
		//TODO check userRights
		String tablename = id.getTableName();
		String sql = "SELECT * FROM `" +tablename+ "` WHERE `PS_" + Character.toUpperCase(tablename.charAt(0))+tablename.substring(1)+"ID` = "+id.getInternalKey()+" LIMIT 1";
		
		@SuppressWarnings("unchecked")
		List<ArachneDataset> temp = (List<ArachneDataset>) this.executeSelectQuery(sql, new ArachneDatasetMapping());
		ArachneDataset temp2 = temp.get(0);
		temp2.setArachneId(id);
		return temp2;
		
	}
}
