package de.uni_koeln.arachne.util;

import java.io.File;

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
	
	public static String getFilenameFromType(String type) {
		String filename = "/WEB-INF/xml/"+ type + ".xml";
		File file = new File(filename);
		if (!file.exists()) {
			filename = "/WEB-INF/xml/fallback.xml";
		}
		System.out.println("filename: " + filename);
		return filename;
	}
}
