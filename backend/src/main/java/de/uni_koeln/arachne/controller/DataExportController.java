package de.uni_koeln.arachne.controller;

import de.uni_koeln.arachne.export.DataExportException;
import de.uni_koeln.arachne.export.DataExportStack;
import de.uni_koeln.arachne.export.DataExportTask;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.export.DataExportFileManager;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;
import static de.uni_koeln.arachne.util.security.SecurityUtils.ADMIN;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        if (task == null) {
            throw new DataExportException("task_not_found", HttpStatus.NOT_FOUND);
        }

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

    @RequestMapping(value = "/cancel/{exportId}", method = RequestMethod.POST, produces={APPLICATION_JSON_UTF8_VALUE})
    ResponseEntity<String> handleAbortTask(
            @PathVariable("exportId") final String exportId
    ) {

        DataExportTask task = dataExportStack.getEnqueuedTaskById(exportId);

        if (task != null) {
            if (!userRightsService.userHasRole(ADMIN) && (userRightsService.getCurrentUser().getId() != task.getOwner().getId())) {
                throw new DataExportException("not_allowed", HttpStatus.FORBIDDEN);
            }

            dataExportStack.dequeueTask(task);

            return ResponseEntity.status(HttpStatus.OK).body("[\"" + exportId + "\"]");
        }


        task = dataExportStack.getRunningTaskById(exportId);

        if (task != null) {
            if (!userRightsService.userHasRole(ADMIN) && (userRightsService.getCurrentUser().getId() != task.getOwner().getId())) {
                throw new DataExportException("not_allowed", HttpStatus.FORBIDDEN);
            }

            dataExportStack.abortTask(task);

            return ResponseEntity.status(HttpStatus.OK).body("[\"" + exportId + "\"]");
        }

        throw new DataExportException("task_not_found", HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/clean", method = RequestMethod.POST)
    ResponseEntity<String> handleClean(
            @RequestBody Map<String, Boolean> settings
    ) throws InterruptedException {


        final Boolean outdated = (settings.get("outdated") == null) ? true : settings.get("outdated");
        final Boolean everyones = (settings.get("everyones") == null) ? false : settings.get("everyones");
        final Boolean finished = (settings.get("finished") == null) ? true : settings.get("finished");

        final Set<String> report = new LinkedHashSet<String>();

        if (everyones && !userRightsService.userHasRole(ADMIN)) {
            throw new DataExportException("no_admin", HttpStatus.FORBIDDEN);
        }

        final User user = everyones ? null : userRightsService.getCurrentUser();

        if (!finished) {
            for (DataExportTask task : dataExportStack.getEnqueuedTasks(user)) {
                dataExportStack.dequeueTask(task);
                report.add(task.uuid.toString());
            }

            for (DataExportTask task : dataExportStack.getRunningTasks(user)) {
                dataExportStack.abortTask(task);
                report.add(task.uuid.toString());
            }

            // since threads may take a moment to close
            Integer i = 0;
            while (dataExportStack.getRunningTasks(user).size() != 0) {
                if (i > 25) {
                    throw new DataExportException("unknown", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Thread.sleep(1000);
            }

        }


        final ArrayList<DataExportTask> finishedTasks = dataExportStack.getFinishedTasks(user, outdated);

        for (DataExportTask task : finishedTasks) {
            try {
                dataExportFileManager.deleteFile(task);
            } catch (DataExportException exception) {
                LOGGER.warn(exception.getMessage());
            }
            dataExportStack.removeFinishedTask(task);
            LOGGER.info("Deleted outdated task: " + task.uuid.toString());
            report.add(task.uuid.toString());
        }

        return ResponseEntity.status(HttpStatus.OK).body(new JSONArray(report.toArray()).toString());

    }

}