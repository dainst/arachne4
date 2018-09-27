package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DataExportExceptionHandler {

    @Autowired
    private transient Transl8Service ts;

    @ExceptionHandler(DataExportException.class)
    public ResponseEntity<String> handlerMyException(DataExportException ex) {

        String message;

        try {
            message = ts.transl8("error_data_export_" + ex.type, ex.lang);
        } catch (Transl8Service.Transl8Exception e) {
            message = "untranslated error: error_data_export_" + ex.type;
        }

        if (!ex.untranslatableContent.equals("")) {
            message += ": " + ex.untranslatableContent;
        }

        return new ResponseEntity(message, ex.getHttpStatus());
    }

}