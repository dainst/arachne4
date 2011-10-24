package de.uni_koeln.arachne.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.util.StrUtils;

public class GenericEntitesMapper implements RowMapper<Map<String,String>> {
	public Map<String,String> mapRow(ResultSet rs, int rownum) throws SQLException {
		Map<String,String> dataset = new Hashtable<String,String>();

		ResultSetMetaData meta = rs.getMetaData();
		int to = meta.getColumnCount();
		for (int i = 1; i <= to; i++) {
			String columnName = meta.getColumnLabel(i);

			//Keys dont Interest the Dataset
			if (columnName.contains("FS_") && columnName.contains("ID")) {
				continue;
			}
			
			//The rest of the Dataset
			String columnValue = rs.getString(columnName);
			if (!StrUtils.isEmptyOrNull(columnValue)) {
				dataset.put(meta.getTableName(i) + "." + columnName, rs.getString(columnName));
			}
		}
		return dataset;
	}
}
