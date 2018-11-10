package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.util.DataExportFileManager;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Paf
 */
@Component
@Scope("prototype")
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

    @Async
    public void run() {

        try {
            // request scope hack (enabling session scope) - needed so the UserRightsService can be used
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
            LOGGER.info("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: RUNNING");
            dataExportFileManager.writeToFile(dataExportTask);
        } catch (Exception e) {
            LOGGER.error("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ERROR: " + e.getClass(), e);
            dataExportTask.error = true;
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: FINISHED");
            dataExportStack.taskIsFinishedListener(dataExportTask);

            // disable request scope hack
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).requestCompleted();
            RequestContextHolder.resetRequestAttributes();
        }

    }

    public void registerListener(DataExportStack stack) {
        this.dataExportStack = stack;
    }

}