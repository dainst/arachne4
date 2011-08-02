package de.uni_koeln.arachne.util;


public class ArachneId {

	/**
	 * This is the outer Arachne entity ID.
	 */
	protected Long arachneEntityID;

	/**
	 * This is the internal table name of Arachne.
	 */
	protected String tableName;

	/**
	 * This is the internal key which is used inside the Arachne.
	 */
	protected Long internalKey;

	/**
	 * Flag indicating if the dataset exists or was deleted.
	 */
	protected boolean isDeleted;

	/**
	 * This Constructor gets Tablename and Internal key as Identification
	 * @param table String Tablename
	 * @param key Long This is the 
	 * @param aeid Long Arachne Identification Number
	 * @param isDeleted boolean is Deleted?
	 */
	public ArachneId(String table,Long key, Long aeid, boolean isdel) {
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