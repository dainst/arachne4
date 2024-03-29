package de.uni_koeln.arachne.export;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author Paf
 * 
 * @param <T> The type the converter handles.
 */

public abstract class BasePdfConverter<T> extends AbstractDataExportConverter<T> {

    public BasePdfConverter() {
        super(MediaType.APPLICATION_PDF);
    }

    protected BaseHtmlConverter<?> getHtmlConverter() {
        BaseHtmlConverter<?> htmlConverter;
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


    protected void writePdf(Writer inStream, OutputStream outStream) {

        final Document w3cDoc = convertStringToDocument(inStream.toString());

        try {
            final PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
            final String baseUrl = "file:" + servletContext.getRealPath("/WEB-INF/dataexport");
            pdfBuilder.withW3cDocument(w3cDoc, baseUrl);
            pdfBuilder.useFont(new FSSupplier<InputStream>() {
                @Override
                public InputStream supply() {
                    return this.getClass().getResourceAsStream("/WEB-INF/dataexport/SourceSansPro-Regular.ttf");
                }
            }, "source-sans", 700, BaseRendererBuilder.FontStyle.NORMAL, false);
            pdfBuilder.toStream(outStream);
            pdfBuilder.run();
        } catch (Exception e) {
            LOGGER.error("PDF could not be created.", e);
            throw new DataExportException("data_export_io_error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Document convertStringToDocument(String content) {
        final W3CDom w3cDom = new W3CDom();
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
            LOGGER.error("PDF could not be created. (JSOUP)", e);
            throw new DataExportException("data_export_io_error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
