package de.uni_koeln.arachne.mapping.jdbc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.service.DataIntegrityLogService;
import de.uni_koeln.arachne.util.JSONUtil;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.sql.SQLToolbox;

/**
 * Maps the {@link ResultSet} of any entity including an optional JSON field. 
 * @author satan
 *
 */
@Configurable(preConstruction=true)
public class GenericEntitiesMapper implements RowMapper<Map<String,String>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericEntitiesMapper.class);

	@Autowired
	private transient DataIntegrityLogService dataIntegrityLogService;
	
	private final transient String jsonField;

	/**
	 * Constructor setting the name of the JSON field.
	 * @param jsonField The name of the field.
	 */
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
			String columnValue = resultSet.getString(columnName);
			if (!StrUtils.isEmptyOrNullOrZero(columnValue)) {
				if (columnName.equals(jsonField)) {
					boolean done = false;
					boolean fixed = false;
					while (!done) {
						try {
							@SuppressWarnings("unchecked")
							final Map<String,String> result = JSONUtil.MAPPER.readValue(columnValue, Map.class);
							LOGGER.debug(result.toString());
							dataset.putAll(result);
							done = true;
							LOGGER.debug(dataset.toString());
						}
						catch (JsonParseException e) {
							if (!fixed) {
								String identifierType = SQLToolbox.generatePrimaryKeyName(resultSet.getMetaData().getTableName(i));
								long identifier = resultSet.getLong(identifierType);
								dataIntegrityLogService.logWarning(identifier, identifierType, "Invalid JSON in DB");
								LOGGER.error("Invalid JSON [" + identifierType + ":" + identifier + "]: " 
										+ columnName + " = " + columnValue + System.lineSeparator() + "Cause: " + e.getMessage());
								columnValue = fixJson(columnValue);
								fixed = true;
							} else {
								done = true;
							}
						} catch (JsonMappingException e) {
							LOGGER.error(e.getMessage(), e);
						} catch (IOException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
				} else {
					dataset.put(meta.getTableName(i) + "." + columnName, columnValue);
				}
			}
		}
		return dataset;
	}

	/**
	 * This method tries to fix the JSON from the DB by escaping quotation marks in JSON values.<br>
	 * Should be removed when/if this gets fixed on the DB side
	 * @param columnValue The invalid JSON.
	 * @return The valid JSON.
	 */
	private String fixJson(final String columnValue) {
		String result = "";
		int start = 0;
		int lastStart = -1;
		int end = 2;
		while (end < columnValue.length() - 2 && start > lastStart) {
			// find value
			lastStart = start;
			start = columnValue.indexOf(":\"", start) + 2;
			end = columnValue.indexOf("\",", start);
			if (end < start) {
				end = columnValue.indexOf("\"}", start);
			}
			String jsonValue = columnValue.substring(start, end);
			result = columnValue.replace(jsonValue, StringEscapeUtils.escapeJson(jsonValue));
		}
		return result;
	}
}
