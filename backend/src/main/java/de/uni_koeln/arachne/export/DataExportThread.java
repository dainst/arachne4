package de.uni_koeln.arachne.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Paf
 */

@Component
@Scope("prototype")
public class DataExportThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("DataExportLogger");

    private DataExportTask dataExportTask;

    private DataExportStack dataExportStack;

    private DataExportFileManager dataExportFileManager;

    public DataExportThread(DataExportTask dataExportTask) {
        this.dataExportTask = dataExportTask;
    }

    @Async
    public void run() {

        try {
            // request scope hack (enabling session scope) - needed so the UserRightsService can be used
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

            LOGGER.info("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: RUNNING");
            dataExportFileManager.writeToFile(dataExportTask);

        } catch (DataExportAbortionException e) {
            LOGGER.error("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ABORTED");
            dataExportTask.error = "aborted";
            //throw new RuntimeException(e);

        } catch (DataExportException e) {
            LOGGER.error("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ERROR: "
                    + e.getMessage() + " - " + e.untranslatableContent, e);
            dataExportTask.error = "error";
            throw new RuntimeException(e);

        } catch (Exception e) {
            LOGGER.error("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ERROR: " + e.getMessage(), e);
            dataExportTask.error = "error";
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

    public void setFileManager(DataExportFileManager dataExportFileManager) {
        this.dataExportFileManager = dataExportFileManager;
    }
}