package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.DataIntegrityLogService;

/*
 * Customized <code>RowMapper</code> to map the SQL query result to an <code>Image</code>.
 */
public class ImageRowMapper implements RowMapper<Image> {
		
	@Autowired
	private transient DataIntegrityLogService dataIntegrityLogService;
	
	@Override
	public Image mapRow(final ResultSet resultSet, final int index) throws SQLException {
		final Image result = new Image();
		String fileName = resultSet.getString(1);
		if (fileName != null) {
			result.setImageSubtitle(fileName.substring(0, fileName.lastIndexOf('.')));
		} else {
			dataIntegrityLogService.logWarning(resultSet.getLong(2), "PS_MARBilderID", "Image without filename.");
		}
		result.setImageId(resultSet.getLong(2));
				
		return result;
	}
}
