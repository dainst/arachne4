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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

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

        exportTable.headers = getCsvHeaders(facets);

        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);

        setProcessors();

        csvHeaders();



        //*/

        handleOnlyFirstPlace = true;
        includeEmptyFacets = true;
        sortFacets = false;
        skipFacets = new ArrayList<>();

        if(entities == null) {
            return;
        }

        for (final SearchHit hit : entities) {

            final List<Object> row = new ArrayList<Object>();

            // there are items wo subtitle and maybe wo title out there...
            String title = (hit.getTitle() == null) ? "" : hit.getTitle().replace("\n", "").replace("\r", "");
            String subtitle = (hit.getSubtitle() == null) ? "" : hit.getSubtitle().replace("\n", "").replace("\r", "");

            row.add(String.valueOf(hit.getEntityId()) );
            row.add(hit.getType());
            row.add(title);
            row.add(subtitle);
            row.add(String.valueOf(hit.getThumbnailId()));

            final HashMap<String, DataExportCell> details = getDetails(hit.getEntityId(), facets);
            for (final String key : details.keySet()) {
                DataExportCell detail = details.get(key);
                row.add(detail.value);
            }
            csvWriter.write(row, csvProcessors);
        }

        writer.close();
        csvWriter.close();

    }
}

