package de.uni_koeln.arachne.util;

import java.util.List;

/**
 * Class implementing string utility functions.
 */
@SuppressWarnings("PMD")
public class StrUtils {	
	/**
	 * This method returns if a <code>String</code> is empty or <code>null</code>.
	 * @param string to test.
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(String string) {
		return string == null || string.isEmpty();
	}
	
	/**
	 * This method returns if a <code>List<String></code> is empty or <code>null</code>.
	 * @param stringlist to test.
	 * @return <code>true</code> or <code>false</code>
	 */
	public static boolean isEmptyOrNull(List<String> stringList) {
		return stringList == null || stringList.isEmpty();
	}

	/**
	 * This method returns if a <code>StringBuffer</code> is empty or <code>null</code>.
	 * @param stringBuffer
	 * @return
	 */
	public static boolean isEmptyOrNull(StringBuffer stringBuffer) {
		return stringBuffer == null || stringBuffer.length() < 1;
	}
}
