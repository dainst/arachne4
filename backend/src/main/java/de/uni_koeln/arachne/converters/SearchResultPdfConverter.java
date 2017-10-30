package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

/**
 * @author Paf
 */
public class SearchResultPdfConverter extends DataExportConverter {

    public SearchResultPdfConverter() { super(MediaType.APPLICATION_PDF); }

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.pdf\"");

        final List<SearchHit> entities = searchResult.getEntities();
        final List<SearchResultFacet> facets = searchResult.getFacets();

        OutputStream outStream = httpOutputMessage.getBody();

        PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
        String elStringo = getAsHtml(entities, facets);

        try {
            pdfBuilder.withHtmlContent(elStringo, "/");
            pdfBuilder.toStream(outStream);
            pdfBuilder.run();
        } catch (Exception e) {
            LOGGER.error("PDF could not be created. Most likely XML error.");
            outStream.write(new String("Sorry, an error appeared during PDF creation.").getBytes());
            e.printStackTrace();
        }

    }


    public String getAsHtml(final List<SearchHit> entities, final List<SearchResultFacet> facets) {

        SearchResultHtmlConverter searchResultHtmlConverter = new SearchResultHtmlConverter();

        searchResultHtmlConverter.injectService(entityService);
        searchResultHtmlConverter.injectService(transl8Service);
        searchResultHtmlConverter.injectService(servletContext);
        searchResultHtmlConverter.injectService(iipService);
        searchResultHtmlConverter.injectService(userRightsService);

        searchResultHtmlConverter.writer = new StringWriter();
        searchResultHtmlConverter.htmlHeader();
        searchResultHtmlConverter.htmlFrontmatter(facets);
        searchResultHtmlConverter.htmlResults(entities, facets);
        searchResultHtmlConverter.htmlFooter();

        return searchResultHtmlConverter.writer.toString();


    }




    @Override
    void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector) {

    }
}
