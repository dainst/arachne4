package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.response.Image;

/*
 * Customized <code>RowMapper</code> to map the SQL query result to an <code>Image</code>.
 */
public class ImageRowMapper implements RowMapper<Image> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageRowMapper.class);
	
	@Override
	public Image mapRow(final ResultSet resultSet, final int index) throws SQLException {
		final Image result = new Image();
		String fileName = resultSet.getString(1);
		if (fileName != null) {
			result.setImageSubtitle(fileName.substring(0, fileName.lastIndexOf('.')));
		} else {
			LOGGER.warn("Data Integrity Warning: Image without filename. PS_MARBilderID: " + resultSet.getLong(2));
		}
		result.setImageId(resultSet.getLong(2));
				
		return result;
	}
}
