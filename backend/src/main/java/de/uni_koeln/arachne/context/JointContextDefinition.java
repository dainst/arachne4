package de.uni_koeln.arachne.context;

import java.util.ArrayList;

/**
 * Definition for internal contexts that are not defined in the SemamticConnections table in the DB but in the XML files.
 */
public class JointContextDefinition {

	/** The context type. */ // why is it called 'id'???
	private String id = "";
	
	/** The description. */
	private String description = "";
	
	/** The 'child table' of the used SQL join. */ // why is it called 'type'???
	private String type = "";
	
	/** The column that defines the order of the records. */
	private String orderBy = "";
	
	/** If the order should be descending. */
	private Boolean orderDescending = false;
	
	/** The criteria to group by. */
	private String groupBy = "";
	
	/** The group name. */
	private String groupName = "";
	
	/** The Standard CIDOC connection type. */
	private String StandardCIDOCConnectionType = "";
	
	/** The field of the parent entry that is used as 'link'. */
	private String connectFieldParent = "";
	
	/** The SQL WHERE clauses used to retrieve the records. */
	private ArrayList<String> wheres = new ArrayList<String>();
	
	/** The list of SQL joins. */
	private ArrayList<JoinDefinition> joins = new ArrayList<JoinDefinition>();

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		if (description != null)
			this.description = description;
	}

	/**
	 * Gets the standard CIDOC connection type.
	 *
	 * @return the standard CIDOC connection type
	 */
	public String getStandardCIDOCConnectionType() {
		return StandardCIDOCConnectionType;
	}

	/**
	 * Sets the standard CIDOC connection type.
	 *
	 * @param standardCIDOCConnectionType
	 *            the new standard CIDOC connection type
	 */
	public void setStandardCIDOCConnectionType(String standardCIDOCConnectionType) {
		if (standardCIDOCConnectionType != null)
			StandardCIDOCConnectionType = standardCIDOCConnectionType;
	}

	/**
	 * Gets the 'child table' of the used SQL join.
	 *
	 * @return the 'child table' of the used SQL join
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the 'child table' of the used SQL join.
	 *
	 * @param type
	 *            the new 'child table' of the used SQL join
	 */
	public void setType(String type) {
		if (type != null)
			this.type = type;
	}

	/**
	 * Gets the field of the parent entry that is used as 'link'.
	 *
	 * @return the field of the parent entry that is used as 'link'
	 */
	public String getConnectFieldParent() {
		return connectFieldParent;
	}

	/**
	 * Sets the field of the parent entry that is used as 'link'.
	 *
	 * @param connectFieldParent
	 *            the new field of the parent entry that is used as 'link'
	 */
	public void setConnectFieldParent(String connectFieldParent) {
		if (connectFieldParent != null)
			this.connectFieldParent = connectFieldParent;
	}

	/**
	 * Verifies that the context definition is valid, by checking 
	 *
	 * @return the boolean
	 */
	public Boolean isValid() {
		return (!(connectFieldParent.isEmpty() || type.isEmpty()));
	}

	/**
	 * Gets the context type.
	 *
	 * @return the context type
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the context type.
	 *
	 * @param id
	 *            the new context type
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the SQL WHERE clauses used to retrieve the records.
	 *
	 * @return the SQL WHERE clauses used to retrieve the records
	 */
	public ArrayList<String> getWheres() {
		return (ArrayList<String>) wheres.clone(); // TODO check if clone is really needed
	}

	/**
	 * Adds a SQL WHERE clause.
	 *
	 * @param where
	 *            the SQL WHERE clause
	 */
	public void addWhere(String where) {
		wheres.add(where);
	}

	/**
	 * Gets the list of SQL joins.
	 *
	 * @return the list of SQL joins
	 */
	public ArrayList<JoinDefinition> getJoins() {
		return (ArrayList<JoinDefinition>) joins.clone(); // TODO check if clone is really needed
	}

	/**
	 * Adds the join.
	 *
	 * @param type
	 *            the type
	 * @param connectFieldParent
	 *            the connect field parent
	 * @param connectFieldChild
	 *            the connect field child
	 */
	public void addJoin(String type, String connectFieldParent, String connectFieldChild) {
		joins.add(new JoinDefinition(type, connectFieldParent, connectFieldChild));
	}

	/**
	 * Gets the criteria to group by.
	 *
	 * @return the criteria to group by
	 */
	public String getGroupBy() {
		return groupBy;
	}

	/**
	 * Sets the criteria to group by.
	 *
	 * @param groupBy
	 *            the new criteria to group by
	 */
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets the group name.
	 *
	 * @param groupName
	 *            the new group name
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Checks if is grouped.
	 *
	 * @return true, if is grouped
	 */
	public boolean isGrouped() {
		return (!((groupName.equals("") || (groupName == null)) || ((groupBy.equals("") || (groupBy == null)))));
	}

	/**
	 * Gets the column that defines the order of the records.
	 *
	 * @return the column that defines the order of the records
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Sets the column that defines the order of the records.
	 *
	 * @param orderBy
	 *            the new column that defines the order of the records
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Gets the if the order should be descending.
	 *
	 * @return the if the order should be descending
	 */
	public Boolean getOrderDescending() {
		return orderDescending;
	}

	/**
	 * Sets the if the order should be descending.
	 *
	 * @param orderDescending
	 *            the new if the order should be descending
	 */
	public void setOrderDescending(Boolean orderDescending) {
		this.orderDescending = orderDescending;
	}
}
