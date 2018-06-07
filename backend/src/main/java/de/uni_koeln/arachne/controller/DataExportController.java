package de.uni_koeln.arachne.controller;

import de.uni_koeln.arachne.converters.DataExportException;
import de.uni_koeln.arachne.converters.DataExportStack;
import de.uni_koeln.arachne.converters.DataExportTask;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.DataExportFilesUtil;
import de.uni_koeln.arachne.util.network.CustomMediaType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static de.uni_koeln.arachne.util.security.SecurityUtils.ADMIN;

/**
 * @author Paf
 */

@Controller
@RequestMapping("/export")
public class DataExportController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogController.class);

    @Autowired
    private transient UserRightsService userRightsService;

    @Autowired
    private transient DataExportStack dataExportStack;

    @Autowired
    private transient DataExportFilesUtil dataExportFilesUtil;

    @RequestMapping(value = "/file/{exportId}", method = RequestMethod.GET)
    public void handleGetExportFile(
            HttpServletResponse response,
            @PathVariable("exportId") final String exportId,
            @RequestHeader(value = "Accept-Language", defaultValue = "de") String headerLanguage
    ) {


        System.out.println("get file named " + exportId);

        try {
            dataExportFilesUtil.getFile(response, exportId);
        } catch (IOException e) {
            throw new DataExportException("io_error:" + e.getMessage(), HttpStatus.FORBIDDEN, headerLanguage);
        }
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    ResponseEntity<String> handleGetExportStatus(
            @RequestHeader(value = "Accept-Language", defaultValue = "de") String headerLanguage
    ) {

        if (!userRightsService.userHasRole(ADMIN)) {
            throw new DataExportException("no_admin", HttpStatus.FORBIDDEN, headerLanguage);
        }

        return ResponseEntity.status(HttpStatus.OK).body(dataExportStack.getStatus().toString());
    }

}