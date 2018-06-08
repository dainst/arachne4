package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.util.DataExportFilesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;


/**
 * @author Paf
 */
public class DataExportThread implements Runnable {

    private DataExportTask dataExportTask;

    private DataExportStack dataExportStack;

    private DataExportFilesUtil dataExportFilesUtil = new DataExportFilesUtil();

    public DataExportThread(DataExportTask dataExportTask) {
        this.dataExportTask = dataExportTask;
    }

    public void run() {
        System.out.println("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: RUNNING");

        try {
            Thread.sleep(3000);
            dataExportFilesUtil.writeToFile(dataExportTask);
        } catch (Exception e) {
            System.out.println("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: ERROR:\n" + e.getMessage());
            //throw new RuntimeException(e);
        } finally {
            System.out.println("DataExport-Thread [" + dataExportTask.uuid.toString() + "]: FINISHED");
            dataExportStack.taskIsFinishedListener(dataExportTask);
        }
    }

    public void registerListener(DataExportStack stack) {
        this.dataExportStack = stack;
    }

}