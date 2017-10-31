package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SearchResult2PdfConverter extends BasePdfConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) { return aClass == SearchResult.class; }

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


}
