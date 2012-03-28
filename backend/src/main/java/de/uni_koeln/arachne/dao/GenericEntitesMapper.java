package de.uni_koeln.arachne.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.util.StrUtils;

public class GenericEntitesMapper implements RowMapper<Map<String,String>> {
	public Map<String,String> mapRow(final ResultSet resultSet, final int rownum) throws SQLException {
		final Map<String,String> dataset = new Hashtable<String,String>();

		final ResultSetMetaData meta = resultSet.getMetaData();
		final int count = meta.getColumnCount();
		for (int i = 1; i <= count; i++) {
			final String columnName = meta.getColumnLabel(i);

			//Keys dont Interest the Dataset
			if (columnName.contains("FS_") && columnName.contains("ID")) {
				continue;
			}
			
			//The rest of the Dataset
			final String columnValue = resultSet.getString(columnName);
			if (!StrUtils.isEmptyOrNull(columnValue)) {
				dataset.put(meta.getTableName(i) + "." + columnName, resultSet.getString(columnName));
			}
		}
		return dataset;
	}
}
