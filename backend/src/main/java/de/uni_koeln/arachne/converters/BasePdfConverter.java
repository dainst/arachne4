package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Paf
 */
public abstract class BasePdfConverter<T> extends AbstractDataExportConverter<T> {

    public BasePdfConverter() { super(MediaType.APPLICATION_PDF); }


    public String getAsHtml(final List<SearchHit> entities, final List<SearchResultFacet> facets) {

        SearchResult2HtmlConverter htmlConverter = new SearchResult2HtmlConverter();

        htmlConverter.injectService(entityService);
        htmlConverter.injectService(transl8Service);
        htmlConverter.injectService(servletContext);
        htmlConverter.injectService(iipService);
        htmlConverter.injectService(userRightsService);

        htmlConverter.writer = new StringWriter();
        htmlConverter.htmlHeader();
        htmlConverter.htmlFrontmatter(facets);
        htmlConverter.htmlResults(entities, facets);
        htmlConverter.htmlFooter();

        return htmlConverter.writer.toString();


    }

    @Override
    void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector) {

    }

}
