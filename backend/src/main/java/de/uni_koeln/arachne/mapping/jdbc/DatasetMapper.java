package de.uni_koeln.arachne.mapping.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetMapper.class);
	
	public Map<String,String> mapRow(final ResultSet resultSet, final int rownum) throws SQLException {
		final Map<String,String> dataset = new Hashtable<String,String>();

		final ResultSetMetaData meta = resultSet.getMetaData();
		final int count = meta.getColumnCount();
		for (int i = 1; i <= count; i++) {
			final String columnName = meta.getColumnLabel(i);

			/*
			//Keys dont Interest the Dataset
			if ((columnName.contains("PS_") || columnName.contains("FS_")) && columnName.contains("ID")) {
				continue;
			}
			*/
			//The rest of the Dataset
			final String columnValue = resultSet.getString(columnName);
			if (columnName.contains("ArachneEntityIdentitficaton")) {
				LOGGER.debug(meta.getTableName(i) + "." + columnName + " ," + resultSet.getString(columnName));
				continue;
			}			
						
			if (!StrUtils.isEmptyOrNullOrZero(columnValue)) {
				dataset.put(meta.getTableName(i) + "." + columnName, resultSet.getString(columnName));
			}
		}
		return dataset;
	}
}
