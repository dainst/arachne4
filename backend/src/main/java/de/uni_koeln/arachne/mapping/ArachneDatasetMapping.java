package de.uni_koeln.arachne.mapping;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;


/**
 * Mapping result set to a ArachneDataset abstract object
 * Suits for Bauwerk, Bauwerksteil, Topographie, Objekt, Releif, Realie, Sammlung
 * @author Rasmus Krempel
 */
public class ArachneDatasetMapping implements RowMapper<Map<String,String>> {

	public Map<String,String> mapRow(ResultSet rs, int rownum) throws SQLException {
		Map<String,String> dataset = new Hashtable<String,String>();

		ResultSetMetaData meta = rs.getMetaData();
		int to = meta.getColumnCount();
		for (int i = 1; i <= to; i++) {
			String columnName = meta.getColumnLabel(i);

			//Keys dont Interest the Dataset
			if ((columnName.contains("PS_") || columnName.contains("FS_")) && columnName.contains("ID")) {
				continue;
			}
			// Description and Last Modified
			/*
			if (columnName.contains("Kurzbeschreibung")){
				ds.setTitle(rs.getString(columnName));
				continue;
			}
			if (columnName.equals("lastModified")){
				ds.setLastModified(rs.getDate(columnName));
				continue;
			}

			//Administration information
			if (columnName.contains("Arbeitsnotiz") ){
				ds.setAdminstrationInformation("Arbeitsnotiz", rs.getString(columnName));
				continue;
			}
			if (columnName.contains("DatensatzGruppe") ){
				ds.setAdminstrationInformation("DatensatzGruppe", rs.getString(columnName));
				continue;
			}
			if (columnName.contains("Bearbeiter") ){
				ds.setAdminstrationInformation("Bearbeiter", rs.getString(columnName));
				continue;
			}
			if (columnName.contains("oaipmhset") ){
				ds.setAdminstrationInformation("oaipmhset", rs.getString(columnName));
				continue;
			}

			if (columnName.contains("Korrektor") ){
				ds.setAdminstrationInformation("Korrektor", rs.getString(columnName));
				continue;
			}
			 */
			//The rest of the Dataset
			String columnValue = rs.getString(columnName);
			if (!columnValue.isEmpty()) {
				dataset.put(meta.getTableName(i) + "." + columnName, rs.getString(columnName));
			}
		}
		return dataset;
	}
}
