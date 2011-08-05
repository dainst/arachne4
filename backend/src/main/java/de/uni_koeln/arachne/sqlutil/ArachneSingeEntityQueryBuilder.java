package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.util.ArachneId;

/**
 * This class Constructs a Query for a Singe Entity
 * @author Rasmus Krempel
 *
 */
public class ArachneSingeEntityQueryBuilder extends AbstractArachneSQLBuilder {
	
	protected ArachneId id;
	protected ArachneSQLRightsConditionBuilder rcb;
	
	public ArachneSingeEntityQueryBuilder(ArachneId ident) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		id = ident;
		table = id.getTableName();
		//Limits the Resultcount to 1
		limit1 = true;
		rcb = new ArachneSQLRightsConditionBuilder(table);
		//The Primary key Identification condition
		Condition cnd = new Condition();
		cnd.setOperator("=");
		cnd.setPart1(ArachneSQLToolbox.getQualifiedFieldname(table,ArachneSQLToolbox.generatePrimaryKeyName(table) ));
		cnd.setPart2(id.getInternalKey().toString());
		conditions.add(cnd);
	}
	
	@Override
	protected String buildSQL(){
		sql += "SELECT * FROM `"+table+"` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += this.appendLimitOne();
		sql += ";";
		return sql;	
	}

}
