package de.uni_koeln.arachne.util;


public class ArachneId {

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
	protected transient boolean isDeleted;

	/**
	 * This Constructor gets Tablename and Internal key as Identification
	 * @param table String Tablename
	 * @param key Long This is the 
	 * @param aeid Long Arachne Identification Number
	 * @param isdel is Deleted?
	 */
	public ArachneId(final String table, final Long key, final Long aeid, final boolean isdel) {
		arachneEntityID = aeid;
		tableName = table;
		internalKey = key;
		isDeleted = isdel;
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
		return isDeleted;
	}
}