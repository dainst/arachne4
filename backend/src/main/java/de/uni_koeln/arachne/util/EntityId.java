package de.uni_koeln.arachne.util;


public class EntityId {

	/**
	 * This is the outer Arachne entity ID.
	 */
	protected transient Long arachneEntityID;

	/**
	 * This is the internal table name of Arachne.
	 */
	protected transient String tableName;

	/**
	 * This is the internal key which is used inside the Arachne.
	 */
	protected transient Long internalKey;

	/**
	 * Flag indicating if the dataset exists or was deleted.
	 */
	protected transient boolean deleted;

	/**
	 * This Constructor gets Tablename and Internal key as Identification
	 * @param tableName String Tablename
	 * @param internalKey Long This is the 
	 * @param arachneEntityID Long Arachne Identification Number
	 * @param deleted is Deleted?
	 */
	public EntityId(final String tableName, final Long internalKey, final Long arachneEntityID, final boolean deleted) {
		this.arachneEntityID = arachneEntityID;
		this.tableName = tableName;
		this.internalKey = internalKey;
		this.deleted = deleted;
	}

	/**
	 * Internal (why <code>public</code> if function is internal???) function that gets the Missing Data
	 */
	public Long getInternalKey() {
		return internalKey;
	}

	public String getTableName() {
		return tableName;
	}

	public Long getArachneEntityID() {
		return arachneEntityID;
	}

	public boolean isDeleted() {
		return deleted;
	}
}