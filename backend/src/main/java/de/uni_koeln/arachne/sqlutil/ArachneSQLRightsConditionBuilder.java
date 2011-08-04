package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.util.ArachneUserRightsSingleton;

/**
 * This Object Builds up the User Rights Queston upon the User Rights Service.
 * The userrights consist out of a list of Conditions
 * @author Rasmus Krempel
 *
 */
public class ArachneSQLRightsConditionBuilder {

	
	private List<String> permissiongroups;
	private String tableName;
	
	public ArachneSQLRightsConditionBuilder(String tn) {
		this.tableName =tn;
	}
	
	/**
	 * Builds the <code>Condition</code>s out of the permissiongroups the user is in 
	 * @return List of <code>Condition</code> Objects that ensure the user gets what he is allowed to see
	 */
	private List<Condition> buildConditions(){
		
		List<Condition> conds = new ArrayList<Condition>();
		
		for (String perm : permissiongroups) {
			Condition cnd = new Condition();
			
			cnd.setPart1( ArachneSQLToolbox.getQualifiedFieldname(tableName, "DatensatzGruppe"+ArachneSQLToolbox.ucfirst(tableName)));
			cnd.setPart2("\""+perm +"\"");
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
		ArachneUserRightsSingleton userRights = ArachneUserRightsSingleton.getInstance(); 
		//in This case The User is Authorized to see Everything
		if(userRights.isAuthorizedForAllGroups()){
			return result;
		}
		else{
			//Get the Permission Groups
			permissiongroups = userRights.getUserGroups();
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
					result = result + " OR ";
				}
				result+= cnd.toString();
			}
			result = result + ")";
		}
		return result;
	}
	
	
}
