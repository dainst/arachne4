package de.uni_koeln.arachne.util;

/**
 * Class implementing string utility functions.
 */
public class StrUtils {
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
}
