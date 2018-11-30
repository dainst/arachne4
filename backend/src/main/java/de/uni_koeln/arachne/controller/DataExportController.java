package de.uni_koeln.arachne.controller;

import de.uni_koeln.arachne.converters.DataExportException;
import de.uni_koeln.arachne.converters.DataExportStack;
import de.uni_koeln.arachne.converters.DataExportTask;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.DataExportFileManager;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;
import static de.uni_koeln.arachne.util.security.SecurityUtils.ADMIN;

/**
 * @author Paf
 */

@Controller
@RequestMapping("/export")
public class DataExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger("DataExportLogger");

    @Autowired
    private transient UserRightsService userRightsService;

    @Autowired
    private transient DataExportStack dataExportStack;

    @Autowired
    private transient DataExportFileManager dataExportFileManager;

    @Autowired
    private ContentNegotiationManager contentNegotiationManager;

    @RequestMapping(value = "/file/{exportId}", method = RequestMethod.GET)
    public void handleGetExportFile(
            @PathVariable("exportId") final String exportId,
            @RequestHeader(value = "Accept-Language", defaultValue = "de") String headerLanguage,
            HttpServletResponse response
    ) {

        LOGGER.debug("get file named " + exportId);

        final DataExportTask task = dataExportStack.getFinishedTaskById(exportId);

        final InputStream fileStream = dataExportFileManager.getFile(task);
        final HttpHeaders headers = new HttpHeaders();
        response.setHeader("Content-Type", task.getMediaType().toString() + "; charset=utf-8");
        response.setHeader("Content-Length", Long.toString(dataExportFileManager.getFileSize(task)));
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", dataExportFileManager.getFileName(task)));

        response.setStatus(HttpStatus.OK.value());
        try {
            IOUtils.copy(fileStream, response.getOutputStream());
            response.flushBuffer();
            dataExportStack.removeFinishedTask(task);

        } catch (IOException e) {
            e.printStackTrace();
            throw new DataExportException("io_error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET, produces={APPLICATION_JSON_UTF8_VALUE})
    ResponseEntity<String> handleGetExportStatus() {

        if (!userRightsService.userHasRole(ADMIN)) {
            throw new DataExportException("no_admin", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.status(HttpStatus.OK).body(dataExportStack.getStatus().toString());
    }


    @RequestMapping(value = "/types", method = RequestMethod.GET, produces={APPLICATION_JSON_UTF8_VALUE})
    ResponseEntity<String> handleGetMediaTypes() {

        final Map<String, MediaType> mediaTypeList =
                contentNegotiationManager
                        .getStrategy(ParameterContentNegotiationStrategy.class)
                        .getMediaTypes();

        final HashMap<String, String> collectedTypes = new HashMap<String, String>();
        for (String mKey : mediaTypeList.keySet()) {
            if (!mKey.equals("xml") && !mKey.equals("json")) {
                collectedTypes.put(mKey, mediaTypeList.get(mKey).toString());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new JSONObject(collectedTypes).toString());
    }

    @RequestMapping(value = "/clean", method = RequestMethod.GET)
    ResponseEntity<String> handleClean() {

        if (!userRightsService.userHasRole(ADMIN)) {
            throw new DataExportException("no_admin", HttpStatus.FORBIDDEN);
        }

        final ArrayList<DataExportTask> outdatedTasks = dataExportStack.getOutdatedTasks();

        final JSONArray report = new JSONArray();

        for (DataExportTask task : outdatedTasks) {
            try {
                dataExportFileManager.deleteFile(task);
            } catch (DataExportException exception) {
                LOGGER.warn(exception.getMessage());
            }
            dataExportStack.removeFinishedTask(task);
            LOGGER.info("Deleted outdated task: " + task.uuid.toString());
            report.put(task.uuid.toString());
        }

        return ResponseEntity.status(HttpStatus.OK).body(report.toString());

    }

}