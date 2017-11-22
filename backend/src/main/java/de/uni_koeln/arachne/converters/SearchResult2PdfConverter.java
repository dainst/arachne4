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
import java.io.StringWriter;
import java.util.List;

public class SearchResult2PdfConverter extends BasePdfConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) { return aClass == SearchResult.class; }

    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) {
        try {
            httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.pdf\""); // @ todo reactivate
            final List<SearchHit> entities = searchResult.getEntities();
            final List<SearchResultFacet> facets = searchResult.getFacets();
            OutputStream outStream = httpOutputMessage.getBody();

            SearchResult2HtmlConverter htmlConverter = getHtmlConverter();
            htmlConverter.writer = new StringWriter();
            htmlConverter.htmlHeader();
            htmlConverter.htmlFrontmatter("Search Result", htmlConverter.facetList2String(facets));
            htmlConverter.htmlResults(entities, facets);
            htmlConverter.htmlFooter();
            writePdf((StringWriter) htmlConverter.writer, outStream);
            htmlConverter.writer.close();

        } catch (Exception e) {
            e.printStackTrace(); // @ Todo
        }




    }


}
