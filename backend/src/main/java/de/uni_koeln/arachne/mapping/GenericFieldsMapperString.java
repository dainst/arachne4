package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

public class GenericFieldsMapperString implements RowMapper<List<String>> {
	private int lastRow = 0;	
	
	public GenericFieldsMapperString(int n) {
		lastRow = n + 1;
	}
		
	@Override
	public List<String> mapRow(ResultSet resultSet, int i) throws SQLException {
		List<String> result = new ArrayList<String>();
		for (int row = 1; row < lastRow; row++) {
			result.add(resultSet.getString(row));
		}
		
		if (result.isEmpty()) {
			return null;
		}
		
		return result;
	}
}
