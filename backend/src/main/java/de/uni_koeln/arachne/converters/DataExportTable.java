package de.uni_koeln.arachne.converters;

import java.util.ArrayList;
import java.util.TreeSet;

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




}
