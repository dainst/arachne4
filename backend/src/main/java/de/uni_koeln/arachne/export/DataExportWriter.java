package de.uni_koeln.arachne.export;

import java.io.*;

public class DataExportWriter extends FilterWriter {

    private DataExportTask task;
    private Writer innerWriter;

    public DataExportWriter(DataExportTask task, Writer out) throws UnsupportedEncodingException {
        super(out);
        this.task = task;
        this.innerWriter = out;
    }

    public void write(int c) throws IOException {
        if (task.aborted) {
            super.close();
            throw new DataExportAbortionException();
        }
        super.write(c);
    }

    public String toString() {
        return innerWriter.toString();
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        if (task.aborted) {
            super.close();
            throw new DataExportAbortionException();
        }
        super.write(cbuf, off, len);
    }

    public void write(String str, int off, int len) throws IOException {
        if (task.aborted) {
            super.close();
            throw new DataExportAbortionException();
        }
        super.write(str, off, len);
    }
}
