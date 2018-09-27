package de.uni_koeln.arachne.util;

import de.uni_koeln.arachne.controller.CatalogController;
import de.uni_koeln.arachne.converters.DataExportTask;
import de.uni_koeln.arachne.converters.DataExportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DataExportFileManager {

    //@Value("${dataExportPath:'/tmp'}")
    private String dataExportPath = "/tmp";

    public InputStream getFile(DataExportTask task) {

        final File file = new File(getFileName(task));

        byte[] fileContent = null;

        if(!file.exists()){
            throw new DataExportException("not_found", file.toString(), HttpStatus.NOT_FOUND, "DE"); // @ TODO right language
        }

        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataExportException("io_error", file.toString(), HttpStatus.INTERNAL_SERVER_ERROR, "DE"); // @ TODO right language
        }

    }

    public void deleteFile(DataExportTask task) {
        final Path path = Paths.get(getFileName(task));

        try {
            Files.delete(path);
        } catch (NoSuchFileException x) {
            throw new DataExportException("io_error_missing", path.toString(), HttpStatus.INTERNAL_SERVER_ERROR, "DE");
        } catch (IOException x) {
            throw new DataExportException("io_error_access", path.toString(), HttpStatus.INTERNAL_SERVER_ERROR, "DE");
        }

    }

    public long getFileSize(DataExportTask task) {
        final String fileName = getFileName(task);
        try {
            return Files.size(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataExportException("io_error", fileName, HttpStatus.INTERNAL_SERVER_ERROR, "DE"); // @ TODO right language
        }
    }

    public String getFileName(DataExportTask task) {
        final String extension = task.getMediaType().getSubtype().toString();
        return dataExportPath + "/export-" + task.uuid.toString() + "." + extension;
    }

    public void writeToFile(DataExportTask task) throws Exception {
        try {
            final File file = new File(getFileName(task));
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            file.createNewFile();
            task.perform(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new DataExportException("io_error", getFileName(task), HttpStatus.INTERNAL_SERVER_ERROR, "DE"); // @ TODO right language
        }
    }
}
