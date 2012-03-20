package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;

public class OrtContextualizer extends GenericSQLContextualizer {
	public OrtContextualizer(EntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		super(genericSQLService);
		tableName = "ort";
		joinTableName = "ortsbezug_leftjoin_ort";
	}
}
