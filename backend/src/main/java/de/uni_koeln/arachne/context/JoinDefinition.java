package de.uni_koeln.arachne.context;

/**
 * Class to hold information on how to construct the SQL joins for
 * 'JointContexts'.
 * 
 * @author Reimar Grabowski
 *
 */
public class JoinDefinition {

	/**
	 * The type of the context (which is the SQL table name of the 'child
	 * table').
	 */
	private String type = "";

	/**
	 * The SQL column in the parent table that 'connects' the records.
	 */
	private String connectFieldParent = "";

	/**
	 * The SQL column in the parent table that 'connects' the records.
	 */
	private String connectFieldChild = "";

	/**
	 * Instantiates a new join definition.
	 *
	 * @param type
	 *            the type
	 * @param connectFieldParent
	 *            the connect field parent
	 * @param connectFieldChild
	 *            the connect field child
	 */
	public JoinDefinition(String type, String connectFieldParent, String connectFieldChild) {
		this.setType(type);
		this.setConnectFieldParent(connectFieldParent);
		this.setConnectFieldChild(connectFieldChild);
	}

	/**
	 * Gets the type of the context (which is the SQL table name of the 'child
	 * table').
	 *
	 * @return the type of the context (which is the SQL table name of the
	 *         'child table')
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of the context (which is the SQL table name of the 'child
	 * table').
	 *
	 * @param type
	 *            the new type of the context (which is the SQL table name of
	 *            the 'child table')
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the SQL column in the parent table that 'connects' the records.
	 *
	 * @return the SQL column in the parent table that 'connects' the records
	 */
	public String getConnectFieldParent() {
		return connectFieldParent;
	}

	/**
	 * Sets the SQL column in the parent table that 'connects' the records.
	 *
	 * @param connectFieldParent
	 *            the new SQL column in the parent table that 'connects' the
	 *            records
	 */
	public void setConnectFieldParent(String connectFieldParent) {
		if (connectFieldParent != null)
			this.connectFieldParent = connectFieldParent;
	}

	/**
	 * Gets the SQL column in the parent table that 'connects' the records.
	 *
	 * @return the SQL column in the parent table that 'connects' the records
	 */
	public String getConnectFieldChild() {
		return connectFieldChild;
	}

	/**
	 * Sets the SQL column in the parent table that 'connects' the records.
	 *
	 * @param connectFieldChild
	 *            the new SQL column in the parent table that 'connects' the
	 *            records
	 */
	public void setConnectFieldChild(String connectFieldChild) {
		if (connectFieldChild != null)
			this.connectFieldChild = connectFieldChild;
	}
}
