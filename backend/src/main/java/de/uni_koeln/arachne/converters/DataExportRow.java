package de.uni_koeln.arachne.converters;

import java.util.LinkedHashMap;

public class DataExportRow extends LinkedHashMap<String, DataExportCell> {

    private DataExportTable _table;

    public DataExportRow(DataExportTable table) {
        this._table = table;
    }

    public String getColumnName() {
        return getColumnName("", false);
    }

    /**
     * creates a unique columns name for a given row (this), so it can be used as key.
     * also registers the usage of this name for this table if desired
     *
     * @param col
     * @param register
     * @return
     */
    public String getColumnName(String col, Boolean register) {
        while (containsKey(col)) {
            col = col + "$";
        }

        if (col == null) {
            col = "";
        }

        if (register) {
            _table.headers.add(col);
        }

        return col;
    }


    public String getColumnName(String col) {
        return getColumnName(col, true);
    }

    public DataExportCell put(String key, String label, String value) {
        return super.put(getColumnName(key), new DataExportCell(label, value));
    }

    public DataExportCell put(String key, String value) {
        return super.put(getColumnName(key), new DataExportCell(key, value));
    }


    public DataExportCell putHeadline(String value) {
        return super.put(getColumnName(), new DataExportCell("", value, true));
    }

    public DataExportCell putHeadline(String key, String value) {
        return super.put(getColumnName(key), new DataExportCell("", value, true));
    }
}
