package de.uni_koeln.arachne.sqlutil;
/**
 * This Object Describes a Connection by a Coomeaning of heit fields
 *Example
 */
public class TableConnectionDescription {
	//Name of The first Table
	protected String table1;
	//Name of the second Table 
	protected String table2;
	//Fieldname of Field1 or PrimaryKey or ForeignKey
	protected String field1;
	//Fieldname of field2 or PrimaryKey or ForeignKey
	protected String field2;
	//Is it a Connection by Crosstable
	protected boolean connectionByCrosstable;
	//The name of the Crosstable
	protected String crossTableName;
	
	public TableConnectionDescription() {

	}
	/**
	 * 
	 * @param table1 table1
	 * @param field1 field1
	 * @param table2 table2
	 * @param field2 field2
	 */
	public TableConnectionDescription(String table1,String field1,String table2,String field2) {
		
		this.table1 =table1;
		this.table2 =table1;
		this.field1 =table1;
		this.field2 =table1;
		
	}
	/**
	 * 
	 * @param table1 table1
	 * @param field1 field1
	 * @param table2 table2
	 * @param field2 field2
	 * @param crossTableName CrossTable name
	 */
	public TableConnectionDescription(String table1,String field1,String table2,String field2,String crossTableName ) {
		
		this.table1 =table1;
		this.table2 =table1;
		this.field1 =table1;
		this.field2 =table1;
		this.crossTableName = crossTableName;
		
	}
	
	
	
	/**
	 * Checks for the tablename if one of the Things Lists this
	 * @param tbname The name of The Table to check
	 * @return true if tbname is described in this connection
	 */
	public boolean linksTable(String tbname){
		if(tbname.equals(table1) || tbname.equals(table2)){
			return true;
		}else{
			return false;
		}	
	}
	
	//Setter
	public void setCrosstableName(String crosstableName) {
		this.crossTableName = crosstableName;
		if(crosstableName == null || crosstableName.isEmpty()){
			connectionByCrosstable = false;
		}else{
			connectionByCrosstable = true;
		}	
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
	public void setField2(String field2) {
		this.field2 = field2;
	}
	public void setTable1(String table1) {
		this.table1 = table1;
	}
	public void setTable2(String table2) {
		this.table2 = table2;
	}
	
	//Getter
	public boolean isConnectionByCrosstable() {
		return connectionByCrosstable;
	}
	public String getCrosstableName() {
		return crossTableName;
	}
	public String getField1() {
		return field1;
	}
	public String getField2() {
		return field2;
	}
	public String getTable1() {
		return table1;
	}
	public String getTable2() {
		return table2;
	}
}
