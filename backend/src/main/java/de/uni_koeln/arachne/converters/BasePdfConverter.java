package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;


import de.uni_koeln.arachne.response.search.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;


/**
 * @author Paf
 */
public abstract class BasePdfConverter<T> extends AbstractDataExportConverter<T> {

    public BasePdfConverter() { super(MediaType.APPLICATION_PDF); }



    public BaseHtmlConverter getHtmlConverter() {
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

    public void writePdf(StringWriter inStream, OutputStream outStream) throws IOException {
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

    @Override
    public void serializePlaces(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {
        // dont't care about this baby
    }

}
