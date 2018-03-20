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
	 * @return {@code this}
	 */
	public JointContextDefinition setDescription(String description) {
		if (description != null)
			this.description = description;
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setStandardCIDOCConnectionType(String standardCIDOCConnectionType) {
		if (standardCIDOCConnectionType != null)
			StandardCIDOCConnectionType = standardCIDOCConnectionType;
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setType(String type) {
		if (type != null)
			this.type = type;
		return this;
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
	 * @return {@code this}   
	 */
	public JointContextDefinition setConnectFieldParent(String connectFieldParent) {
		if (connectFieldParent != null)
			this.connectFieldParent = connectFieldParent;
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Gets the SQL WHERE clauses used to retrieve the records.
	 *
	 * @return the SQL WHERE clauses used to retrieve the records
	 */
	public ArrayList<String> getWheres() {
		return wheres;
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
		return joins;
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
	 * @return {@code this}
	 */
	public JointContextDefinition addJoin(String type, String connectFieldParent, String connectFieldChild) {
		joins.add(new JoinDefinition(type, connectFieldParent, connectFieldChild));
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setGroupName(String groupName) {
		this.groupName = groupName;
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setOrderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
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
	 * @return {@code this}
	 */
	public JointContextDefinition setOrderDescending(Boolean orderDescending) {
		this.orderDescending = orderDescending;
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((StandardCIDOCConnectionType == null) ? 0 : StandardCIDOCConnectionType.hashCode());
		result = prime * result + ((connectFieldParent == null) ? 0 : connectFieldParent.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((groupBy == null) ? 0 : groupBy.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((joins == null) ? 0 : joins.hashCode());
		result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
		result = prime * result + ((orderDescending == null) ? 0 : orderDescending.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((wheres == null) ? 0 : wheres.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof JointContextDefinition)) {
			return false;
		}
		JointContextDefinition other = (JointContextDefinition) obj;
		if (StandardCIDOCConnectionType == null) {
			if (other.StandardCIDOCConnectionType != null) {
				return false;
			}
		} else if (!StandardCIDOCConnectionType.equals(other.StandardCIDOCConnectionType)) {
			return false;
		}
		if (connectFieldParent == null) {
			if (other.connectFieldParent != null) {
				return false;
			}
		} else if (!connectFieldParent.equals(other.connectFieldParent)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (groupBy == null) {
			if (other.groupBy != null) {
				return false;
			}
		} else if (!groupBy.equals(other.groupBy)) {
			return false;
		}
		if (groupName == null) {
			if (other.groupName != null) {
				return false;
			}
		} else if (!groupName.equals(other.groupName)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (joins == null) {
			if (other.joins != null) {
				return false;
			}
		} else if (!joins.equals(other.joins)) {
			return false;
		}
		if (orderBy == null) {
			if (other.orderBy != null) {
				return false;
			}
		} else if (!orderBy.equals(other.orderBy)) {
			return false;
		}
		if (orderDescending == null) {
			if (other.orderDescending != null) {
				return false;
			}
		} else if (!orderDescending.equals(other.orderDescending)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (wheres == null) {
			if (other.wheres != null) {
				return false;
			}
		} else if (!wheres.equals(other.wheres)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JointContextDefinition [" + (id != null ? "id=" + id + ", " : "")
				+ (description != null ? "description=" + description + ", " : "")
				+ (type != null ? "type=" + type + ", " : "") + (orderBy != null ? "orderBy=" + orderBy + ", " : "")
				+ (orderDescending != null ? "orderDescending=" + orderDescending + ", " : "")
				+ (groupBy != null ? "groupBy=" + groupBy + ", " : "")
				+ (groupName != null ? "groupName=" + groupName + ", " : "")
				+ (StandardCIDOCConnectionType != null
						? "StandardCIDOCConnectionType=" + StandardCIDOCConnectionType + ", " : "")
				+ (connectFieldParent != null ? "connectFieldParent=" + connectFieldParent + ", " : "")
				+ (wheres != null ? "wheres=" + wheres + ", " : "") + (joins != null ? "joins=" + joins : "") + "]";
	}
}
