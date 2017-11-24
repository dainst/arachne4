package de.uni_koeln.arachne.converters;

public class DataExportSet {

    public DataExportSet() {};

    public DataExportSet(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public DataExportSet(String name, String value, Boolean isHeadline) {
        this.name = name;
        this.value = value;
        this.isHeadline = isHeadline;
    }

    public String name;
    public String value;
    public boolean isHeadline;
}
