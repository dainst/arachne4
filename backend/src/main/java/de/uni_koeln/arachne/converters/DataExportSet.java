package de.uni_koeln.arachne.converters;

public class DataExportSet {

    public DataExportSet() {};

    public DataExportSet(String index, String name, String value) {
        this.index = index;
        this.name = name;
        this.value = value;
    }

    public DataExportSet[] children;

    public String index;
    public String name;
    public String value;
}
