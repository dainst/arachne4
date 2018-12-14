package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Base64;

/**
 * @author Paf
 */

public class Catalog2PdfConverter extends BasePdfConverter<Catalog> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == Catalog.class;
    }

    @Override
    protected void writeInternal(Catalog catalog, HttpOutputMessage httpOutputMessage) throws IOException {
        enqueueIfHuge(catalog, 50);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalog.pdf\"");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "base64");
        convert(new DataExportConversionObject(catalog), Base64.getEncoder().wrap(httpOutputMessage.getBody()));
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final Catalog catalog = conversionObject.getCatalog();
        BaseHtmlConverter htmlConverter = getHtmlConverter();
        htmlConverter.initializeExport(catalog);
        htmlConverter.writer = new StringWriter(); //new DataExportWriter(task, );
        htmlConverter.htmlHeader();
        htmlConverter.htmlFrontmatter(htmlConverter.markdown2html(catalog.getRoot().getText()));
        htmlConverter.htmlCatalog(catalog);
        htmlConverter.htmlFooter();
        writePdf((StringWriter) htmlConverter.writer, outputStream);
        htmlConverter.writer.close();
    }
}