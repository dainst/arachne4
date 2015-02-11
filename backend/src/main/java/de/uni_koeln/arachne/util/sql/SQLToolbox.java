/**
 * 
 */
package de.uni_koeln.arachne.util.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * This Toolbox contains a few Static Methods that do Standard string conversions, Like enclosing in backticks etc.
 * This class is dedicated to catch all the Special cases in the Database, so if there are special cases this class should know about it.
 * @author Rasmus Krempel
 *
 */
public class SQLToolbox { // NOPMD
	
	private static final Map<String, String> PRIMARY_KEY_NAMES = new HashMap<String, String>();
	static {
		PRIMARY_KEY_NAMES.put("marbilder", "PS_MARBilderID");
		PRIMARY_KEY_NAMES.put("zenon", "zenonid");
		PRIMARY_KEY_NAMES.put("arachneentitydegrees", "ArachneEntityID");
	}
	
	/**
	 * Asserts the Name of the Primary key by the Tabename it comes from
	 * @param tablename An Arachne internal Tablename example: bauwerk.
	 * @return The name of the Primary key of that Table example PS_BauwerkID
	 */
	public static String generatePrimaryKeyName(final String tablename){
		final String primaryKeyName = PRIMARY_KEY_NAMES.get(tablename);
		if (primaryKeyName == null) {
			final String newPrimaryKeyName = "PS_" + ucFirst(tablename) + "ID";
			PRIMARY_KEY_NAMES.put(tablename, newPrimaryKeyName);
			return newPrimaryKeyName;
		} else {
			return primaryKeyName; 
		}
	}
	
	/**
	 * Asserts the Name of the foreign key by the Tabename it comes from
	 * @param tablename An Arachne internal Tablename example: objekt
	 * @return The name of the Foreign key of that Table  example: PS_ObjektID
	 */
	public static String generateForeignKeyName(final String tablename){
		
		if ("marbilder".equals(tablename)) {
			return "FS_MARBilderID";
		}		
		return "FS_" + ucFirst(tablename)+"ID";
	}
	
	/**
	 * Returns The name of the Field that Contains the Userrights Group. 
	 * If You are Looking for a Place to Put AnException from the Norm. This is the Right Place to Put it.
	 * @param tableName The Table you want to Know the Userrights Group Field from. 
	 * @return The Field name the User Rights Group is stored in
	 */
	public static String generateDatasetGroupName(final String tableName) {
		if ("marbilder".equals(tableName)) {
			return "DatensatzGruppeMARBilder";
		} else {
			return "DatensatzGruppe"+tableName.substring(0,1).toUpperCase()+tableName.substring(1);
		}
	}
	
	/**
	 * Just encloses a Sting in Backticks
	 * @param inputString a String example: Stuff
	 * @return a String with backticks example: `Stuff`
	 */
	public static String addBackticks(final String inputString) {
		return "`" + inputString + "`";
	} 
	
	/**
	 * Consrutcts a Exact Identifier of a Table and a Fieldname
	 * @param table example bauwerk
	 * @param field example Architect
	 * @return `bauwerk`.`Architect`
	 */
	public static String getQualifiedFieldname(final String table, final String field){
		return addBackticks(table) + "." + addBackticks(field);
	}

	/**
	 * Capitalises the First Charakter of a String
	 * @param inputString a String example: bauwerk
	 * @return example : Bauwerk
	 */
	public static String ucFirst(final String inputString) {
		return Character.toUpperCase(inputString.charAt(0)) + inputString.substring(1);
	}
}
