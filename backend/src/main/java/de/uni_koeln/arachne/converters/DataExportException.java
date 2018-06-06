package de.uni_koeln.arachne.converters;


import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;

public class DataExportException extends RuntimeException {

    @Autowired
    private transient Transl8Service ts;

    private String lang = "de";
    private String type = "";
    private HttpStatus status;

    public DataExportException(String type, HttpStatus status, String lang) {
        this.lang = lang;
        this.type = type;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return "error_data_export_" + type;
//        try {
//            return ts.transl8("error_data_export_" + type, lang);
//        } catch (Transl8Service.Transl8Exception e) {
//            return "error_data_export_" + type;
//        }
    }

    public HttpStatus getHttpStatus() {
        return status;
    }


}
