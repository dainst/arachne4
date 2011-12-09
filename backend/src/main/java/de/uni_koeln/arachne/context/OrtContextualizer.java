package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;

public class OrtContextualizer extends LeftJoinTableContextualizer {
	public OrtContextualizer(EntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		super(arachneEntityIdentificationService, genericSQLService);
		tableName = "ort";
		joinTableName = "ortsbezug_leftjoin_ort";
	}
}
