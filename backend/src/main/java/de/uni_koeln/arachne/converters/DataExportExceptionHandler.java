package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.converters.dataExportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class DataExportExceptionHandler {

    @ExceptionHandler(dataExportException.class)
    public ResponseEntity<String> handlerMyException(dataExportException ex) {
        return new ResponseEntity(ex.getMessage(), ex.getHttpStatus());
    }

}