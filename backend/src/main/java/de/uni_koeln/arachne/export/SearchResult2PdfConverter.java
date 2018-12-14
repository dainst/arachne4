package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;

/**
 * @author Paf
 */

public class SearchResult2PdfConverter extends BasePdfConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException {
        enqueueIfHuge(searchResult, 0);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.pdf\"");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "base64");

        convert(new DataExportConversionObject(searchResult), Base64.getEncoder().wrap(httpOutputMessage.getBody()));
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final SearchResult searchResult = conversionObject.getSearchResult();
        final List<SearchHit> entities = searchResult.getEntities();
        final List<SearchResultFacet> facets = searchResult.getFacets();
        BaseHtmlConverter htmlConverter = getHtmlConverter();
        htmlConverter.initializeExport(transl8("search_result_for") + " " + task.getConversionName());
        //final StringWriter internalWriter =
        htmlConverter.writer = new StringWriter();
        htmlConverter.htmlHeader();
        htmlConverter.htmlFrontmatter();
        htmlConverter.htmlResults(entities, facets);
        htmlConverter.htmlFooter();
        writePdf((StringWriter) htmlConverter.writer, outputStream);
        htmlConverter.writer.close();
    }
}