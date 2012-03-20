package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;

public class DatierungContextualizer extends GenericSQLContextualizer {
	public DatierungContextualizer(EntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		super(genericSQLService);
		tableName = "datierung";
		joinTableName = null;
	}
}
