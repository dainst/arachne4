package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.response.ArachneDataset;
/**
 * This Class gets The Sub Projects Informations. This differs form the ArachneSingleEntityQueryBuilder that it doesnt check the User Rights.
 *  these do normally not Exist in Subprojects
 *	The Main Reason this is Implementet is for Objekt, Objekt has a lot of Subprojects which which are Optional. 
 */
public class SingleEntitySubTablesQueryBuilder extends AbstractSQLBuilder {

	
	public SingleEntitySubTablesQueryBuilder(ArachneDataset ads,TableConnectionDescription tcd) {
		sql = "";
		conditions = new ArrayList<Condition>(1);

		String targetTable;
		String targetField;
		
		String sourceTable;
		String sourceField;		
		if(tcd.getTable1().equals(ads.getArachneId().getTableName())){
			targetTable =tcd.getTable2();
			targetField =tcd.getField2();
			sourceTable =tcd.getTable1();
			sourceField  =tcd.getField1();
		}else{
			targetTable =tcd.getTable1();
			targetField =tcd.getField1();
			sourceTable =tcd.getTable2();
			sourceField  =tcd.getField2();
		}
		table = targetTable;
		String info;
		if(sourceField.equals("PrimaryKey"))
			 info = ads.getArachneId().getInternalKey().toString();
		else
			 info = ads.getField(sourceTable+"."+sourceField);
		
		
		
		
		//Limits the Result count to 1
		limit1 = true;
		//Building condition to find Subproject
		Condition cnd = new Condition();
		cnd.setOperator("=");
		if(targetField.equals("PrimaryKey"))
			cnd.setPart1(SQLToolbox.getQualifiedFieldname(targetTable, SQLToolbox.generatePrimaryKeyName(targetTable)));
		else
			cnd.setPart1(SQLToolbox.getQualifiedFieldname(targetTable,targetField));
		cnd.setPart2(info);
		conditions.add(cnd);
	}
	
	
	@Override
	protected String buildSQL() {
		sql += "SELECT * FROM `" + table + "` WHERE 1";
		sql += this.buildAndConditions();
		if(limit1)
			sql += this.appendLimitOne();
		sql += ";";
		return sql;	
	}

}
