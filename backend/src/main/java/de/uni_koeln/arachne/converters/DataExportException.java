package de.uni_koeln.arachne.converters;


import org.springframework.http.HttpStatus;


public class DataExportException extends RuntimeException {


    public String lang = "de";
    public String type = "";
    public HttpStatus status;
    public String untranslatableContent = "";

    public DataExportException(String type, HttpStatus status, String lang) {
        this.type = type;
        this.status = status;
        this.lang = lang;
    }

    public DataExportException(String type, String untranslatableContent, HttpStatus status, String lang) {
        this.type = type;
        this.status = status;
        this.untranslatableContent = untranslatableContent;
        this.lang = lang;
    }

    @Override
    public String getMessage() {
        return "error_data_export_" + type;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }


}
