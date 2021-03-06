package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Paf
 */

public class Catalog2HtmlConverter extends BaseHtmlConverter<Catalog> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == Catalog.class;
    }

    @Override
    protected void writeInternal(Catalog catalog, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        enqueueIfHuge(catalog, 150);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/html");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalog.html\"");
        convert(new DataExportConversionObject(catalog), httpOutputMessage.getBody());
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final Catalog catalog = conversionObject.getCatalog();
        this.writer = new DataExportWriter(task, new OutputStreamWriter(outputStream));
        initializeExport(catalog);
        htmlHeader();
        htmlFrontmatter(markdown2html(catalog.getRoot().getText()));
        htmlCatalog(catalog);
        htmlFooter();
        writer.close();
    }

}
