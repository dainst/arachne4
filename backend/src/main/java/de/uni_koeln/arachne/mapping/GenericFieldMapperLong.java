package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class GenericFieldMapperLong implements RowMapper<Long> {
	@Override
	public Long mapRow(final ResultSet resultSet, final int index) throws SQLException {
		return resultSet.getLong(1);
	}
}
