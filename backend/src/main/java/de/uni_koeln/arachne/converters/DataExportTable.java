package de.uni_koeln.arachne.converters;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExportTable extends ArrayList<DataExportRow>  {

    // table headers
    public TreeSet<String> headers;

    //meta
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
     * this transforms intermediate colnames like fuckyou$$$ into better names like fuckyou_3
     * also removes trailing @-symbols wich where used to keep important columns at the beginning
     * we don't name them directliy like this to avoid massive regexing when when calling
     * DataExportRow.getColumnName
     *
     *
     * @param colName
     * @return
     */
    private String _sanitizeColumnName(String colName) {
        final Matcher regexMatcher = Pattern.compile("(\\$+)$").matcher(colName);
        final StringBuffer resultString = new StringBuffer();

        while (regexMatcher.find()) {
            regexMatcher.appendReplacement(resultString, "_" + String.valueOf(1 + regexMatcher.group(1).length()));
        }
        regexMatcher.appendTail(resultString);

        String result = resultString.toString();

        if ((result.substring(0, 1).equals("@"))) {
            result = result.substring(1);
        }

        if ((result.substring(0, 1).equals("@"))) {
            result = result.substring(1);
        }

        return result;
    }

    /**
     * resturn sanitezed columns names
     * @return
     */
    public ArrayList<String> getColumns() {
        final ArrayList<String> tableHeaders = new ArrayList<String>(){};

        for (String header : headers) {
            tableHeaders.add(_sanitizeColumnName(header));
        }

        return tableHeaders;
    }


}
