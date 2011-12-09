package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;

public class LiteraturContextualizer extends LeftJoinTableContextualizer {
	public LiteraturContextualizer(EntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		super(arachneEntityIdentificationService, genericSQLService);
		tableName = "literatur";
		joinTableName = "literaturzitat_leftjoin_literatur";
	}
}
