package de.uni_koeln.arachne.converters;

import java.util.ArrayList;
import java.util.TreeSet;

public class DataExportTable extends ArrayList<DataExportRow>  {

    public TreeSet<String> headers;


    public DataExportTable() {
        super();
        headers = new TreeSet<String>();
    }


    public DataExportRow newRow() {
        DataExportRow newRow = new DataExportRow(this);
        return newRow;
    }


}
