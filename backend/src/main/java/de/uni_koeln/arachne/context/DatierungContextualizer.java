package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;

public class DatierungContextualizer extends LeftJoinTableContextualizer {
	public DatierungContextualizer(ArachneEntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		super(arachneEntityIdentificationService, genericSQLService);
		tableName = "datierung";
		joinTableName = null;
	}
}
