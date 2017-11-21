package de.uni_koeln.arachne.converters;

public class DataExportSet {

    public DataExportSet() {};

    public DataExportSet(String index, String name, String value) {
        this.index = index;
        this.name = name;
        this.value = value;
    }

    public DataExportSet(String index, String name, String value, Boolean isHeadline) {
        this.index = index;
        this.name = name;
        this.value = value;
        this.isHeadline = isHeadline;
    }

    public String index;
    public String name;
    public String value;
    public boolean isHeadline;
}
