package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.*;
import java.util.List;

public class Catalog2PdfConverter extends BasePdfConverter<Catalog> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == Catalog.class;
    }

    @Override
    protected void writeInternal(Catalog catalog, HttpOutputMessage httpOutputMessage) throws IOException {
        enqueIfHuge(catalog, 50);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalog.pdf\"");
        convert(new DataExportConversionObject(catalog), httpOutputMessage.getBody());
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final Catalog catalog = conversionObject.getCatalog();
        BaseHtmlConverter htmlConverter = getHtmlConverter();
        htmlConverter.initializeExport(catalog);
        htmlConverter.writer = new StringWriter();
        htmlConverter.htmlHeader();
        htmlConverter.htmlFrontmatter(htmlConverter.markdown2html(catalog.getRoot().getText()));
        htmlConverter.htmlCatalog(catalog);
        htmlConverter.htmlFooter();
        writePdf((StringWriter) htmlConverter.writer, outputStream);
        htmlConverter.writer.close();
    }
}