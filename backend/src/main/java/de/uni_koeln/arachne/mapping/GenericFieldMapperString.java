package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class GenericFieldMapperString implements RowMapper<String> {
	@Override
	public String mapRow(final ResultSet resultSet, final int index) throws SQLException {
		final String result = resultSet.getString(1);
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}
}
