package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class GenericFieldMapperInteger implements RowMapper<Integer> {

	@Override
	public Integer mapRow(final ResultSet resultSet, final int index) throws SQLException {
		return resultSet.getInt(1);
	}

}
