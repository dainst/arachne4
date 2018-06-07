package de.uni_koeln.arachne.util;


import de.uni_koeln.arachne.converters.DataExportException;
import de.uni_koeln.arachne.converters.DataExportStack;
import de.uni_koeln.arachne.converters.DataExportTask;
import de.uni_koeln.arachne.service.UserRightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Service
public class DataExportFilesUtil {

    @Autowired
    public transient DataExportStack dataExportStack;

    @Autowired
    private transient UserRightsService userRightsService;

    public void getFile(HttpServletResponse response, String identifier) throws IOException {

        final DataExportTask task = dataExportStack.getFinishedTaskById(identifier);

        // chceck if user is right

        final File file = new File("/tmp/export-" + identifier + ".txt");

        if(!file.exists()){
            throw new DataExportException("file_not_available", HttpStatus.GONE, "DE");
        }


        //response.setContentType(mimeType);

        //response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));

        response.setContentLength((int)file.length());

        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        //Copy bytes from source to destination(outputstream in this example), closes both streams.
        FileCopyUtils.copy(inputStream, response.getOutputStream());

    }
}
