package de.uni_koeln.arachne.mapping;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.util.StrUtils;

public class GenericEntitiesMapper implements RowMapper<Map<String,String>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericEntitiesMapper.class);
	
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
			if (!StrUtils.isEmptyOrNullOrZero(columnValue)) {
				if (columnName.equals(jsonField)) {
					try {
						@SuppressWarnings("unchecked")
						final Map<String,String> result = new ObjectMapper().readValue(columnValue, Map.class);
						LOGGER.debug(result.toString());
						dataset.putAll(result);
						LOGGER.debug(dataset.toString());
					}
					catch (JsonParseException e) {
						LOGGER.error(e.getMessage());
					}
					catch (JsonMappingException e) {
						LOGGER.error(e.getMessage());
					}
					catch (IOException e) {
						LOGGER.error(e.getMessage());
					}
				} else {
					dataset.put(meta.getTableName(i) + "." + columnName, columnValue);
				}
			}
		}
		return dataset;
	}
}
