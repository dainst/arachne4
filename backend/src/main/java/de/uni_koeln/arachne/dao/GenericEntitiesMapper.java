package de.uni_koeln.arachne.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.util.StrUtils;

public class GenericEntitiesMapper implements RowMapper<Map<String,String>> {
	private final transient String jsonField;

	public GenericEntitiesMapper(final String jsonField) {
		this.jsonField = jsonField;
	}
	
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
				if (columnName.equals(jsonField)) {
					try {
						Map<String,String> result = new ObjectMapper().readValue(columnValue, HashMap.class);
						System.out.println(result);
						System.out.println(dataset);
						dataset.putAll(result);
					}
					catch (JsonParseException e) {
						System.out.println("FUCKERS!!! " + e.getMessage());
					}
					catch (JsonMappingException e) {
						System.out.println("FUCKERS!!! " + e.getMessage());
					}
					catch (IOException e) {
						System.out.println("FUCKERS!!! " + e.getMessage());
					}
				} else {
					dataset.put(meta.getTableName(i) + "." + columnName, columnValue);
				}
			}
		}
		return dataset;
	}
}
