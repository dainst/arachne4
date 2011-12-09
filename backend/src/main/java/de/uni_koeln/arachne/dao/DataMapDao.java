package de.uni_koeln.arachne.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.DatasetMapper;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.sqlutil.SingleEntityQueryBuilder;
import de.uni_koeln.arachne.sqlutil.SingleEntitySubTablesQueryBuilder;
import de.uni_koeln.arachne.sqlutil.TableConnectionDescription;
import de.uni_koeln.arachne.util.ArachneId;
/**
 * Querys the DataBase and retrives the Result as Key/Value Map
 */
@Repository("arachneDataMapDao")
public class DataMapDao extends SQLDao {
		/**
		 * Gets a map of values by Id
		 * @param id instance of <code>ArachneId</code> 
		 * @return a Simple representation of a Map<String,String> or <code>null</code>.
		 */
		public Map<String, String> getById(ArachneId id) {
			
			SingleEntityQueryBuilder queryBuilder = new SingleEntityQueryBuilder(id);
			
			String sql = queryBuilder.getSQL();
			// TODO remove debug
			System.out.println("ArachneDatamapDao: " + queryBuilder.getSQL());
			
			@SuppressWarnings("unchecked")
			List<Map<String,String>> temp = (List<Map<String, String>>) this.executeSelectQuery(sql, new DatasetMapper());
			if (temp != null) {
				if (!temp.isEmpty()) {
					Map<String,String> map =  temp.get(0);
					return map;
				}
			}
			return null;
		}
		
		/**
		 * Gets a subdataset for a main dataset (Objekt -> Objektplastik) by using <code>ArachneSingleEntitySubTablesQueryBuilder</code> for query Building
		 * @param ds Dataset for which the subdataset should be retrieved
		 * @param tdesc instance of <code>TableConnectionDescription</code> which represents the Connection between the Dataset and the Subdataset 
		 * @return <code>Map<String,String></code> that contains the Description of the Subdataset, caution! The Subdataset is NOT automatically appended to the Dataset.
		 */
		public Map<String, String> getBySubDataset(Dataset ds,TableConnectionDescription tdesc ) {
			
			SingleEntitySubTablesQueryBuilder qB = new SingleEntitySubTablesQueryBuilder(ds,tdesc);
			
			String sql = qB.getSQL();
			System.out.println(sql);
			@SuppressWarnings("unchecked")
			List<Map<String,String>> temp = (List<Map<String, String>>) this.executeSelectQuery(sql, new DatasetMapper());
			
			//achneDataset out= new  ArachneDataset();
			
			Map<String,String> map;  
			if(temp.isEmpty())
				map = new HashMap<String,String>();
			else
				map=  temp.get(0);
			return map;	
		}
	}