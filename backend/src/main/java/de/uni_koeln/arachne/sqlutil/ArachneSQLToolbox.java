/**
 * 
 */
package de.uni_koeln.arachne.sqlutil;

/**
 * This Toolbox contains a few Static Methods
 * they are representig a few annoying SQL String Conversion Problems etc
 * @author archaeopool
 *
 */
public class ArachneSQLToolbox {
	/**
	 * Asserts the Name of the Primary key by the Tabename it comes from
	 * @param tablename An Arachne internal Tablename example: bauwerk
	 * @return The name of the Primary key of that Table example PS_BauwerkID
	 */
	public static String generatePrimaryKeyName(String tablename){
		
		return "PS_" + ucfirst(tablename)+"ID";
		
	}
	/**
	 * Asserts the Name of the foreign key by the Tabename it comes from
	 * @param tablename An Arachne internal Tablename example: objekt
	 * @return The name of the Foreign key of that Table  example: PS_ObjektID
	 */
	public static String generateForeignKeyName(String tablename){
		
		return "FS_" + ucfirst(tablename)+"ID";
		
	}
	/**
	 * Just encloses a Sting in Backticks
	 * @param in a String example: Stuff
	 * @return a String with backticks example: `Stuff`
	 */
	public static String addBackticks(String in){
		
		return "`"+in+"`";
		
	} 
	/**
	 * Consrutcts a Exact Identifier of a Table and a Fieldname
	 * @param table example bauwerk
	 * @param field example Architect
	 * @return `bauwerk`.`Architect`
	 */
	public static String getQualifiedFieldname(String table,String field){
		
		return addBackticks(table) + "." + addBackticks(field);
		
	}
	/**
	 * Capitalises the First Charakter of a String
	 * @param in a String example: bauwerk
	 * @return example : Bauwerk
	 */
	public static String ucfirst(String in) {

		return Character.toUpperCase(in.charAt(0))+in.substring(1);
	}
}
