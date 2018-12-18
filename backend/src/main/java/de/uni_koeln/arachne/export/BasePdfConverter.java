package de.uni_koeln.arachne.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

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

    protected void writePdf(StringWriter inStream, OutputStream outStream) {
        PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
        try {
            Document w3cDoc = convertStringToDocument(inStream.toString());
            pdfBuilder.withW3cDocument(w3cDoc, "/");
            pdfBuilder.toStream(outStream);
            pdfBuilder.run();
        } catch (Exception e) {
            LOGGER.error("PDF could not be created.", e);
            throw new DataExportException("data_export_io_error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Document convertStringToDocument(String content) {
        W3CDom w3cDom = new W3CDom();
        try {
            return w3cDom.fromJsoup(Jsoup.parse(content));

        // this catches an error which occurs, when in the data something is written like ´<http://www´ which can be
        // mistaken as namespace. This is a known issue in jsoup:
        // https://github.com/jhy/jsoup/issues/848
        // it will be fixed in the next version von jsoup and then, this function can be removed.
        } catch (DOMException e){
            if (e.code == 14) {
                LOGGER.debug("namespace_err prevented");
                content = content.replaceAll("<([\\w\\d]+:)","< $1");
                return w3cDom.fromJsoup(Jsoup.parse(content));
            }
            throw e;
        }
    }


}
