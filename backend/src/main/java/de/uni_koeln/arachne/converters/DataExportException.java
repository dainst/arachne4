package de.uni_koeln.arachne.converters;


import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;


public class DataExportException extends RuntimeException {

    @Autowired
    private transient Transl8Service ts;

    private String _lang = "de";
    private String _type = "";
    private HttpStatus _status;

    public DataExportException(String type, HttpStatus status, String lang) {
        _type = type;
        _status = status;
        _lang = lang;
    }

    @Override
    public String getMessage() {

        return "error_data_export_" + _type;

//        try { @ TODO make this work
//            return ts.transl8("error_data_export_" + _type, _lang);
//        } catch (Transl8Service.Transl8Exception e) {
//            return "error_data_export_" + _type;
//        }
    }

    public HttpStatus getHttpStatus() {
        return _status;
    }


}
