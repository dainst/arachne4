package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This Object Builds up the User Rights Queston upon the User Rights Service.
 * The userrights consist out of a list of Conditions
 * @author Rasmus Krempel
 *
 */
@Configurable(preConstruction=true)
public class SQLRightsConditionBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLRightsConditionBuilder.class);
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	private transient Set<DatasetGroup> permissiongroups;
	private transient final String tableName;
	private transient final UserAdministration user;
	private transient List<String> exludedTables;
		
	@Autowired
	@Value("#{config.authFreeTables}")
	public void setExcludeTables(final String authFreeTablesCSS) {
		final List<String> authFreeTables = StrUtils.getCommaSeperatedStringAsList(authFreeTablesCSS);
		exludedTables = new ArrayList<String>();
		if (authFreeTables != null) {
			exludedTables.addAll(authFreeTables);
		} else {
			LOGGER.error("Problem configuring authentication free tables. List is: " + authFreeTables);
		}
	}
	
	public SQLRightsConditionBuilder(final String tableName) {
		this.tableName = tableName;
		this.user = userRightsService.getCurrentUser();
	}
	
	/**
	 * Builds the <code>Condition</code>s out of the permissiongroups the user is in 
	 * @return List of <code>Condition</code> Objects that ensure the user gets what he is allowed to see
	 */
	private List<Condition> buildConditions() {
		
		final List<Condition> conditions = new ArrayList<Condition>();
		
		for (final DatasetGroup group : permissiongroups) {
			final Condition condition = new Condition();
			
			condition.setPart1( SQLToolbox.getQualifiedFieldname(tableName, "DatensatzGruppe"+SQLToolbox.ucfirst(tableName)));
			condition.setPart2("\""+ group.getName() +"\"");
			condition.setOperator("=");
			conditions.add( condition);
			
		}
		return conditions;
	}
	
	/**
	 * Gets the users permissions and converts them in an SQL-Snipplet 
	 * ready to append it to the SQL <code>WHERE</code> statement.
	 * @return A String that represents the user permission SQl statements its empty if the User is allowed to see everything
	 */
	public String getUserRightsSQLSnipplett() {
		final StringBuffer result = new StringBuffer(16);
		//in This case The User is Authorized to see Everything
		if (user.isAll_groups() || exludedTables.contains(tableName)) {
			return result.toString();
		} else {
			//Get the Permission Groups
			permissiongroups = user.getDatasetGroups();
			if (!permissiongroups.isEmpty()) {
				//Convert the Permission Groups to real conditions
				final List<Condition> conditions = buildConditions();

				//Sum up and Build the String
				result.append(" AND (");
				boolean first = true;
				for (final Condition cnd : conditions) {
					if (first) {
						first = false;
					} else {
						result.append(" OR");
					}
					result.append(cnd.toString());
				}
				result.append(')');
			}
		}
		
		return result.toString();
	}
	
}