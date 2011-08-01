package de.uni_koeln.arachne.Util;


public class ArachneId {

	/**
	 * This is the outer Arachne Entity ID.
	 */
	protected Long arachneEntityID;
	
	/**
	 * This is the Internal Table name of Arachne.
	 */
	protected String tableName;
	
	/**
	 * This is the Internal Key which is used inside the Arachne.
	 */
	protected Long internalKey;
	
	/**
	 * Note if the Dataset exists
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
		internalKey=key;
		isDeleted = isdel;
		
	}
	/**
	 * Internal Function that gets the Missing Data
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
