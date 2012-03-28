package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

/**
 * Generic <code>RowMapper</code> that maps the result rows of an SQL query to a <code>String</code> list.
 * This class is only useful for queries that return multiple fields and must not be created via the parameterless 
 * default constructor but the provided custom one. 
 */
public class GenericFieldsMapperString implements RowMapper<List<String>> {
	/**
	 * Number of rows to add to the result list.
	 */
	private transient int lastRow = 0;
	
	/**
	 * Custom constructor initializing the <code>lastRow</code> property.
	 * <br>
	 * IMPORTANT: Always use this constructor to create instances of this class.
	 * @param rowCount The number of rows to map from the <code>ResultSet</code> to the result list.
	 */
	public GenericFieldsMapperString(int rowCount) {
		lastRow = rowCount + 1;
	}
	
	/**
	 * Method to map the rows from the <code>ResultSet</code> to the result list.
	 * @return A string list containing the results of the SQL query.
	 */
	@Override
	public List<String> mapRow(final ResultSet resultSet, final int index) throws SQLException {
		final List<String> result = new ArrayList<String>();
		for (int row = 1; row < lastRow; row++) {
			result.add(resultSet.getString(row));
		}
		
		if (result.isEmpty()) {
			return null;
		}
		
		return result;
	}
}