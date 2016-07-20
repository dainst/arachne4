package de.uni_koeln.arachne.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author Daniel de Oliveira
 */
@Controller
public class InfoController {

    private String buildNumber=null;

    @RequestMapping(value="/info",
            method= RequestMethod.GET,
            produces = {APPLICATION_JSON_UTF8_VALUE})
    public @ResponseBody
    ResponseEntity<String> handleInfoRequest() {

        String responseBody = "{}";
        if (buildNumber!=null)
            responseBody="{ \"buildNumber\" : \""+buildNumber+"\"}";

        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    /**
     * @param buildNumber
     */
    @Value("${buildNumber:}")
    public void setBooksPath(String buildNumber){
        if (!buildNumber.equals(""))
            this.buildNumber = buildNumber;
    }
}
