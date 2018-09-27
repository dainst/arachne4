package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.util.DataExportFileManager;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import de.uni_koeln.arachne.mapping.hibernate.User;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

/**
 * @author Paf
 */
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DataExportThread implements Runnable {

    private DataExportTask dataExportTask;

    private DataExportStack dataExportStack;

    private DataExportFileManager dataExportFileManager = new DataExportFileManager();

    private User user;

    private RequestAttributes context;
    //private final Map context = ThreadContext.getContext();

    public DataExportThread(DataExportTask dataExportTask, RequestAttributes context) {
        this.context = context;
        this.dataExportTask = dataExportTask;
    }

    public void run() {
        RequestContextHolder.setRequestAttributes(context);

        System.out.println("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: RUNNING");

        try {
            Thread.sleep(3000);
            dataExportFileManager.writeToFile(dataExportTask);
        } catch (Exception e) {

            System.out.println("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ERROR: " + e.getClass()
                    + "\n" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            System.out.println("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: FINISHED");
            dataExportStack.taskIsFinishedListener(dataExportTask);
            ThreadContext.clearAll();
        }
    }

    public void registerListener(DataExportStack stack) {
        this.dataExportStack = stack;
    }

}