package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;

/**
 * This Object Builds up the User Rights Queston upon the User Rights Service.
 * The userrights consist out of a list of Conditions
 * @author Rasmus Krempel
 *
 */
public class SQLRightsConditionBuilder {

	private transient Set<DatasetGroup> permissiongroups;
	private transient String tableName;
	private transient UserAdministration user;
	private transient List<String> exludedTables = new ArrayList<String>();
	
	
	public SQLRightsConditionBuilder(String tn, UserAdministration user) {
		this.tableName = tn;
		this.user = user;
		// TODO find a better way to exclude specific tables
		exludedTables.add("ArachneSemanticConnection");
		exludedTables.add("datierung");
		exludedTables.add("literatur");
		exludedTables.add("literaturzitat_leftjoin_literatur");
		exludedTables.add("ort");
		exludedTables.add("ortsbezug_leftjoin_ort");
		exludedTables.add("rezeptionsobjekte");
		exludedTables.add("sammler");
	}
	
	/**
	 * Builds the <code>Condition</code>s out of the permissiongroups the user is in 
	 * @return List of <code>Condition</code> Objects that ensure the user gets what he is allowed to see
	 */
	private List<Condition> buildConditions(){
		
		List<Condition> conds = new ArrayList<Condition>();
		
		for (DatasetGroup group : permissiongroups) {
			Condition cnd = new Condition();
			
			cnd.setPart1( SQLToolbox.getQualifiedFieldname(tableName, "DatensatzGruppe"+SQLToolbox.ucfirst(tableName)));
			cnd.setPart2("\""+ group.getName() +"\"");
			cnd.setOperator("=");
			conds.add( cnd);
			
		}
		return conds;
	}
	
	/**
	 * Gets the users permissions and converts them in an SQL-Snipplet 
	 * ready to append it to the SQL <code>WHERE</code> statement.
	 * @return A String that represents the user permission SQl statements its empty if the User is allowed to see everything
	 */
	public String getUserRightsSQLSnipplett(){
		String result = "";
		//in This case The User is Authorized to see Everything
		if (user.isAll_groups() || exludedTables.contains(tableName)) {
			return result;
		} else {
			//Get the Permission Groups
			permissiongroups = user.getDatasetGroups();
			
			if (!permissiongroups.isEmpty()) {
				//Convert the Permission Groups to real conditions
				List<Condition> conds = buildConditions();

				//Sum up and Build the String
				result = result + " AND (";
				boolean first = true;
				for (Condition cnd : conds) {
					if(first){
						first = false;
					}
					else{
						result = result + " OR";
					}
					result+= cnd.toString();
				}
				result = result + ")";
			}
		}
		return result;
	}	
}