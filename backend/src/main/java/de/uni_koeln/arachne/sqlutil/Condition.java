package de.uni_koeln.arachne.sqlutil;
/**
 * This Class Represent a Condition in a Where Statement in a Query.
 * the 3 Parts of A Statement are in the End Constructed to a Single String.
 * It Repersents a Condition in an SQL Where Statement.
 * 
 * 
 * @author Rasmus Krempel
 *
 */
public class Condition {
	/**
	 * Then Field of the Condition
	 * like: kurzbeschreibungBauwerk, LastModified ......
	 */
	String part1;
	/**
	 * The Value in most Cases
	 * Something like: 100, "%AA%","%A"......
	 */
	String part2;
	/**
	 * The Operator Something 
	 * Like: "LIKE", "NOT LIKE", "IS NOT", "=" ..... 
	 */
	String operator;
	
	/**
	 * Constructor Initialises the Parameters
	 */
	public Condition() {
		part1 = "";
		part2 = "";
		operator = "";
	
	}

	/**
	 * returns the Conditions as String Value. the Condition is made to be Part of an Where Statement.
	 */
	public String toString(){
		return " "+ part1 +" "+ operator + " " + part2;
	}
	/**
	 * Set the Operator
	 * @param operator Operator like : "LIKE", "IS NOT", "=" etc
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	/**
	 * Set the First part of the condition Field etc
	 * @param part1 First part of the Condition normaly a Field name
	 */
	public void setPart1(String part1) {
		this.part1 = part1;
	}
	/**
	 * Set the Target Value of the Condition something like a Value
	 * @param part2
	 */
	public void setPart2(String part2) {
		this.part2 = part2;
	}
	
	
	/**
	 * Get the Operator of the Condition
	 * @return Operator like : "LIKE", "IS NOT", "=" etc
	 */
	public String getOperator() {
		return operator;
	}
	/**
	 * Get the First part of the condition Field etc
	 * @return First part of the Condition normaly a Field name
	 */
	public String getPart1() {
		return part1;
	}
	/**
	 * Get the Target Value of the Condition something like a Value
	 * @return The value of the Condition 
	 */
	public String getPart2() {
		return part2;
	}
	
	
	
}
