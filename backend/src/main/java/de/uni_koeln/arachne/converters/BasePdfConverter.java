package de.uni_koeln.arachne.converters;

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

    public BasePdfConverter() { super(MediaType.APPLICATION_PDF); }



    public SearchResult2HtmlConverter getHtmlConverter() {
        SearchResult2HtmlConverter htmlConverter = new SearchResult2HtmlConverter();

        htmlConverter.injectService(entityService);
        htmlConverter.injectService(transl8Service);
        htmlConverter.injectService(servletContext);
        htmlConverter.injectService(iipService);
        htmlConverter.injectService(userRightsService);
        htmlConverter.injectService(catalogEntryDao);

        return htmlConverter;
    }

    public void writePdf(StringWriter inStream, OutputStream outStream) throws IOException {
        PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
        try {
            //pdfBuilder.withHtmlContent(inStream.toString(), "/"); // to work with streams instead of string conversion?

            W3CDom w3cDom = new W3CDom();
            Document w3cDoc = w3cDom.fromJsoup(Jsoup.parse(inStream.toString()));

            pdfBuilder.withW3cDocument(w3cDoc, "/");
            pdfBuilder.toStream(outStream);
            pdfBuilder.run();
        } catch (Exception e) {
            LOGGER.error("PDF could not be created.");
            throw (IOException) e;
        }

    }

    @Override
    public void serializePlaces(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {
        // dont't care about this baby
    }
}
