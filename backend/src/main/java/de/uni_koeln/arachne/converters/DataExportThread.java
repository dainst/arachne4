package de.uni_koeln.arachne.converters;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;


/**
 * @author Paf
 */
public class DataExportThread implements Runnable {

    private DataExportTask dataExportTask;

    @Autowired
    private DataExportStack dataExportStack;

    public DataExportThread(DataExportTask dataExportTask) {
        this.dataExportTask = dataExportTask;
    }

    public void run() {
        System.out.println("Thread running! Task=" + dataExportTask.name);

        try {
            Thread.sleep(3000);
            //provisional instead of rendering export, just write a file
            final String ts = new Timestamp(System.currentTimeMillis()).toString();
            PrintWriter writer = new PrintWriter("/tmp/export-" + dataExportTask.uuid + ".txt", "UTF-8");
            writer.println("This would be a data export named " + dataExportTask.name);
            writer.close();
        } catch (Exception e) {
            System.out.println("Thread error! Task="  + dataExportTask.name);
            System.out.println(e.getStackTrace());
            throw new RuntimeException("FILEIO ERROR: " + e.getMessage());
        } finally {
            System.out.println("Thread finished! Task="  + dataExportTask.name );
            dataExportStack.taskIsFinishedListener(dataExportTask);
        }
    }

    public void registerListener(DataExportStack stack) {
        this.dataExportStack = stack;
    }

}