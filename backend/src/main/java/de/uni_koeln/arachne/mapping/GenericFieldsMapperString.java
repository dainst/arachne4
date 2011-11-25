package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

public class GenericFieldsMapperString implements RowMapper<List<String>> {
	@Override
	public List<String> mapRow(ResultSet resultSet, int i) throws SQLException {
		System.out.println("GenericFieldsMapperString: ");
		List<String> result = new ArrayList<String>();
		while (resultSet.next()) {
			result.add(resultSet.getString(resultSet.getRow()));
		}
		System.out.println("GenericFieldsMapperString: " + result);
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}
}
