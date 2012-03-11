package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.util.StrUtils;


/**
 * Mapping result set to a ArachneDataset abstract object
 * Suits for Bauwerk, Bauwerksteil, Topographie, Objekt, Releif, Realie, Sammlung
 * <br>
 * If the query result contains the <code>ArachneEntityId</code> is added,
 * @author Rasmus Krempel
 */
public class DatasetMapper implements RowMapper<Map<String,String>> {

	public Map<String,String> mapRow(ResultSet rs, int rownum) throws SQLException {
		Map<String,String> dataset = new Hashtable<String,String>();

		ResultSetMetaData meta = rs.getMetaData();
		int to = meta.getColumnCount();
		for (int i = 1; i <= to; i++) {
			String columnName = meta.getColumnLabel(i);

			//Keys dont Interest the Dataset
			if ((columnName.contains("PS_") || columnName.contains("FS_")) && columnName.contains("ID")) {
				continue;
			}
			
			//The rest of the Dataset
			String columnValue = rs.getString(columnName);
			if (columnName.contains("ArachneEntityIdentitficaton")) {
				System.out.println(meta.getTableName(i) + "." + columnName + " ," + rs.getString(columnName));
				continue;
			}			
						
			if (!StrUtils.isEmptyOrNull(columnValue)) {
				dataset.put(meta.getTableName(i) + "." + columnName, rs.getString(columnName));
			}
		}
		return dataset;
	}
}
