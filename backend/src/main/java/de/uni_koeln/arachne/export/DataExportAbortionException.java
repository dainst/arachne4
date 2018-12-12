package de.uni_koeln.arachne.export;

import org.springframework.http.HttpStatus;

/**
 * @author Paf
 */

public class DataExportAbortionException extends DataExportException {

    public DataExportAbortionException() {
        super("aborted", HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return "error_data_export_abortion";
    }

}
