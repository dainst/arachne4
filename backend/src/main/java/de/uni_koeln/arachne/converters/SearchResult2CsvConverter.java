package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class SearchResult2CsvConverter extends BaseCsvConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");

        final List<SearchResultFacet> facets = searchResult.getFacets();
        final List<SearchHit> entities = searchResult.getEntities();

        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);

        initializeExport("Search Result"); // TODO transl8
        //exportTable.headers = getCsvHeaders(facets);
        serialize(entities);
        csvHeaders();
        csvBody();
        csvFooter();

        csvWriter.close();

    }
}

