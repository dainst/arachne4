package de.uni_koeln.arachne.sqlutil;

import java.util.List;

public abstract class AbstractArachneSQLBuilder {
	protected String sql;
	protected String table;
	protected boolean limit1;
	/**
	 * With And connected Conditions
	 */
	protected List<Condition> conditions;
	
	public String getSQL(){		
		buildSQL();
		return sql;
	}
	protected abstract String buildSQL();
	/**
	 * This Function concats all the Conditions to an SQL Snipplett for the Where Statement
	 * @return An SQL Snipplett with all the Condition as SQl Snipplett
	 */
	protected String buildAndConditions(){
		String result = new String();

		for (Condition cnd : conditions) {

				result+= " AND";
				
			result+=cnd.toString();
		}
		
		return result;
		
	}
	protected String appendLimitOne(){
		if(limit1){
			return " Limit 1";
		}
		else{
			return "";
		}
	}
	
	
	
}
