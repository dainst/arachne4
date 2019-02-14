package de.uni_koeln.arachne.export;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paf
 */

public class DataExportTable extends ArrayList<DataExportRow>  {

	private static final long serialVersionUID = 1L;
	
    // table headers
    public TreeSet<String> headers;

    // meta
    public String title;
    public String timestamp;
    public String user;
    public String author;

    public DataExportTable() {
        super();
        headers = new TreeSet<String>();
    }

    public DataExportRow newRow() {
        DataExportRow newRow = new DataExportRow(this);
        return newRow;
    }

    /**
     * This transforms intermediate column names like fuckyou$$$ into better names like fuckyou_3.
     * It also removes trailing @-symbols which where used to keep important columns at the beginning.
     * We don't name them directly like this to avoid massive regexing when when calling DataExportRow.getColumnName
     *
     * @param colName the column name
     * @return a 'sanitized' column name
     */
    private String _sanitizeColumnName(String colName) {
        final Matcher regexMatcher = Pattern.compile("(\\$+)$").matcher(colName);
        final StringBuffer resultString = new StringBuffer();

        while (regexMatcher.find()) {
            regexMatcher.appendReplacement(resultString, "_" + String.valueOf(1 + regexMatcher.group(1).length()));
        }
        regexMatcher.appendTail(resultString);

        String result = resultString.toString();

        if ((result.startsWith("@"))) {
            result = result.substring(1);
        }
        
        return result;
    }

    /**
     * Returns a 'sanitized' copy of the {@link #headers} field as list.
     *   
     * @return the list of column names
     */
    public ArrayList<String> getColumns() {
        final ArrayList<String> tableHeaders = new ArrayList<String>(){private static final long serialVersionUID = 1L;};

        for (String header : headers) {
            tableHeaders.add(_sanitizeColumnName(header));
        }

        return tableHeaders;
    }


}
