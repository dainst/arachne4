package de.uni_koeln.arachne.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.ArachneDatasetMapping;
import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.sqlutil.ArachneSingleEntityQueryBuilder;
import de.uni_koeln.arachne.util.ArachneId;
/**
 * Retrives Arachne Dataset By ID
 * @author Rasmus Krempel
 *
 */
@Repository("arachneSingleDatasetDao")
public class ArachneSingleDatasetDao extends SQLDao {
	/**
	 * Gets a Dataset by ID
	 * @param id instance of <code>ArachneId</code> 
	 * @return a Simple Representation of an Arachne Dataset.
	 */
	public ArachneDataset getById(ArachneId id) {
		
		ArachneSingleEntityQueryBuilder qB = new ArachneSingleEntityQueryBuilder(id);
		
		String sql = qB.getSQL();

		List<?> temp = this.executeSelectQuery(sql, new ArachneDatasetMapping());
		ArachneDataset temp2 = (ArachneDataset) temp.get(0);
		temp2.setArachneId(id);
		return temp2;	
	}
}
