package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.converters.DataExportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class DataExportExceptionHandler {

    @ExceptionHandler(DataExportException.class)
    public ResponseEntity<String> handlerMyException(DataExportException ex) {
        return new ResponseEntity(ex.getMessage(), ex.getHttpStatus());
    }

}