package de.uni_koeln.arachne.converters;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * I really tried hard to have only one converter for different endpoint,
 * but this is java and it's inherently desperately craving for class madness
 * so I write new classes on and on.
 * I wish it would be only an island.
 */

public class SearchResult2HtmlConverter extends BaseHtmlConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/html");
        //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.html\"");

        final List<SearchHit> entities = searchResult.getEntities();
        final List<SearchResultFacet> facets = searchResult.getFacets();

        setExportMetaData("Search Result"); // todo transl8

        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        htmlHeader();
        htmlFrontmatter(facetList2String(facets));
        htmlResults(entities, facets);
        htmlFooter();
        writer.close();
    }
}
