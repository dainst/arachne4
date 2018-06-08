package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.util.DataExportFileManager;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


/**
 * @author Paf
 */
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DataExportThread implements Runnable {

    private DataExportTask dataExportTask;

    private DataExportStack dataExportStack;

    private DataExportFileManager dataExportFileManager = new DataExportFileManager();

    public DataExportThread(DataExportTask dataExportTask) {
        this.dataExportTask = dataExportTask;
    }

    public void run() {
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
        }
    }

    public void registerListener(DataExportStack stack) {
        this.dataExportStack = stack;
    }

}