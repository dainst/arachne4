package de.uni_koeln.arachne.util;

import java.util.List;

/**
 * Class implementing string utility functions.
 */
public class StrUtils {	
	/**
	 * This method returns if a <code>String</code> is empty or <code>null</code>.
	 * @param string to test.
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(String string) {
		if (string != null) {
			if (string.isEmpty()) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * This method returns if a <code>List<String></code> is empty or <code>null</code>.
	 * @param stringlist to test.
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(List<String> stringList) {
		if (stringList != null) {
			if (stringList.isEmpty()) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}
}
