package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.response.Image;

/*
 * Customized <code>RowMapper</code> to map the SQL query result to an <code>Image</code>.
 */
public class ImageRowMapper implements RowMapper<Image> {
	@Override
	public Image mapRow(final ResultSet resultSet, final int index) throws SQLException {
		final Image result = new Image();
		
		result.setSubtitle(resultSet.getString(1));
		result.setImageId(resultSet.getLong(2));
		
		if (result.getImageId() == null) {
			return null;
		}
		
		return result;
	}
}
