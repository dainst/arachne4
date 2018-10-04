package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.util.DataExportFileManager;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Paf
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DataExportThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("DataExportLogger");

    private DataExportTask dataExportTask;

    private DataExportStack dataExportStack;

    private DataExportFileManager dataExportFileManager = new DataExportFileManager();

    private User user;

    private HttpServletRequest request;

    public DataExportThread(DataExportTask dataExportTask, HttpServletRequest request) {
        this.request = request;
        this.dataExportTask = dataExportTask;
    }

    public void run() {

        final RequestContextListener rcl = new RequestContextListener();
        final ServletContext sc = request.getServletContext();
        rcl.requestInitialized(new ServletRequestEvent(sc, request));

        LOGGER.info("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: RUNNING");

        try {
            Thread.sleep(3000); // DEBUG - remove if feature is complete
            dataExportFileManager.writeToFile(dataExportTask);
        } catch (Exception e) {

            LOGGER.error("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ERROR: " + e.getClass(), e);
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: FINISHED");
            dataExportStack.taskIsFinishedListener(dataExportTask);
            ThreadContext.clearAll();
        }
    }

    public void registerListener(DataExportStack stack) {
        this.dataExportStack = stack;
    }

}