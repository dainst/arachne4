package de.uni_koeln.arachne.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * @author Paf
 */

public abstract class BasePdfConverter<T> extends AbstractDataExportConverter<T> {

    public BasePdfConverter() {
        super(MediaType.APPLICATION_PDF);
    }

    protected BaseHtmlConverter getHtmlConverter() {
        BaseHtmlConverter htmlConverter;
        if (task.getConversionType().equals("searchResult")) {
            htmlConverter = new SearchResult2HtmlConverter();
        } else { // only two types exist
            htmlConverter = new Catalog2HtmlConverter();
        }

        htmlConverter.injectService(entityService);
        htmlConverter.injectService(transl8Service);
        htmlConverter.injectService(servletContext);
        htmlConverter.injectService(iipService);
        htmlConverter.injectService(catalogEntryDao);

        htmlConverter.task = task;

        return htmlConverter;
    }

    protected void writePdf(StringWriter inStream, OutputStream outStream) throws IOException {
        PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
        try {
            W3CDom w3cDom = new W3CDom();
            Document w3cDoc = w3cDom.fromJsoup(Jsoup.parse(inStream.toString()));

            pdfBuilder.withW3cDocument(w3cDoc, "/");
            pdfBuilder.toStream(outStream);
            pdfBuilder.run();
        } catch (Exception e) {
            LOGGER.error("PDF could not be created.", e);
            throw (IOException) e;
        }

    }


}
