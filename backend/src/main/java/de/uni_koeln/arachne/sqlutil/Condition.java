package de.uni_koeln.arachne.sqlutil;
/**
 * This Class Represent a Condition in a Where Statement in a Query
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
	 * Like: LIKE, NOT LIKE, IS NOT, = ..... 
	 */
	String operator;
	
	
	public Condition() {
		part1 = "";
		part2 = "";
		operator = "";
	
	}

	/**
	 * This Returns the Conditions as String Value
	 */
	public String toString(){
		return part1 +" "+ operator + " " + part2;
	}
	/**
	 * Set the Operator
	 * @param operator Operator like : LIKE, IS NOT, = etc
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	/**
	 * Set the First part of the condition Field etc
	 * @param part1 String with the name of part 1
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
	
	
	
	public String getOperator() {
		return operator;
	}
	public String getPart1() {
		return part1;
	}
	public String getPart2() {
		return part2;
	}
	
	
	
}
