package de.uni_koeln.arachne.export;

import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * @author Paf
 */

public class DataExportAbortionException extends DataExportException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DataExportAbortionException() {
        super("aborted", HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return "error_data_export_abortion";
    }

}
