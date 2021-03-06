package de.uni_koeln.arachne.util.sql;

import java.util.ArrayList;

import de.uni_koeln.arachne.response.Dataset;
/**
 * This Class gets The Sub Projects Informations. This differs form the ArachneSingleEntityQueryBuilder that it doesn´t check the User Rights.
 *  these do normally not Exist in Subprojects
 *	The Main reason this is Implementet is for Objekt, Objekt has a lot of Subprojects  which are Optional.
 */
public class SingleEntitySubTablesQueryBuilder extends AbstractSQLBuilder {

	public SingleEntitySubTablesQueryBuilder(final Dataset dataset, final TableConnectionDescription tableConnectionDescription) {
		conditions = new ArrayList<Condition>(1);

		String targetTable;
		String targetField;
		String sourceTable;
		String sourceField;		
		
		if (tableConnectionDescription.getTable1().equals(dataset.getArachneId().getTableName())) {
			targetTable = tableConnectionDescription.getTable2();
			targetField = tableConnectionDescription.getField2();
			sourceTable = tableConnectionDescription.getTable1();
			sourceField = tableConnectionDescription.getField1();
		} else {
			targetTable = tableConnectionDescription.getTable1();
			targetField = tableConnectionDescription.getField1();
			sourceTable = tableConnectionDescription.getTable2();
			sourceField = tableConnectionDescription.getField2();
		}

		table = targetTable;
		String info;
		if ("PrimaryKey".equals(sourceField)) {
			info = dataset.getArachneId().getInternalKey().toString();
		} else {
			if (!sourceField.contains(".")) {
				sourceField = sourceTable + "." + sourceField;
			}
			info = dataset.getField(sourceField);
		}
				
		//Limits the Result count to 1
		limit1 = true;
		//Building condition to find Subproject
		final Condition condition = new Condition();
		condition.setOperator("=");
		if ("PrimaryKey".equals(targetField)) {
			condition.setPart1(SQLToolbox.getQualifiedFieldname(targetTable, SQLToolbox.generatePrimaryKeyName(targetTable)));
		} else {
			condition.setPart1(SQLToolbox.getQualifiedFieldname(targetTable,targetField));
		}
		condition.setPart2("\"" + info + "\"");
		conditions.add(condition);
	}
	
	
	@Override
	protected void buildSQL() {
		final StringBuilder result = new StringBuilder(64).append(sql);
		result.append("SELECT * FROM `");
		result.append(table);
		result.append("` WHERE 1");
		result.append(this.buildAndConditions());
		if (limit1) {
			result.append(this.appendLimitOne());
		}
		result.append(';');
		sql = result.toString();
	}
}
