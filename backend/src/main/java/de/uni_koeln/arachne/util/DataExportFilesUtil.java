package de.uni_koeln.arachne.util;


import de.uni_koeln.arachne.converters.DataExportException;
import de.uni_koeln.arachne.converters.DataExportStack;
import de.uni_koeln.arachne.converters.DataExportTask;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class DataExportFilesUtil {

    @Autowired
    public transient DataExportStack dataExportStack;

    public InputStream getFile(DataExportTask task) {

        final File file = new File(getFileName(task));

        byte[] fileContent = null;

        if(!file.exists()){
            throw new DataExportException("not_found", HttpStatus.NOT_FOUND, "DE"); // @ TODO right language
        }

        try {
            InputStream inputStream = new FileInputStream(file);
            //InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataExportException("io_error", HttpStatus.INTERNAL_SERVER_ERROR, "DE"); // @ TODO right language
        }

    }

    public long getFileSize(DataExportTask task) {
        try {
            return Files.size(Paths.get(getFileName(task)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataExportException("io_error", HttpStatus.INTERNAL_SERVER_ERROR, "DE"); // @ TODO right language
        }
    }

    public String getFileName(DataExportTask task) {
        return "/tmp/export-" + task.uuid.toString() + ".txt";
    }

    public void writeToFile(DataExportTask task) throws Exception {
        //provisional instead of rendering export, just write a file
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(getFileName(task), "UTF-8");
            writer.println("Write DataExport " + task.uuid.toString());
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new Exception("FileIO Error: " + getFileName(task) + "\n" + e.getMessage());
        }
    }
}
