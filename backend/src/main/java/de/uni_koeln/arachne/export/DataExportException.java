package de.uni_koeln.arachne.export;

import org.springframework.http.HttpStatus;

/**
 * @author Paf
 */

public class DataExportException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
    public String type = "";
    public HttpStatus status;
    public String untranslatableContent = "";


    public DataExportException(String type, HttpStatus status) {
        this.type = type;
        this.status = status;
    }

    public DataExportException(String type, String untranslatableContent, HttpStatus status) {
        this.type = type;
        this.status = status;
        this.untranslatableContent = untranslatableContent;
    }

    @Override
    public String getMessage() {
        return "error_data_export_" + type;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }

}
