package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
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

        final List<String> headers = createHeaders(facets);

        final Writer writer = new OutputStreamWriter(httpOutputMessage.getBody());
        final CellProcessor[] processors = getProcessors(headers);
        final CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        csvWriter.writeHeader(headers.toArray(new String[headers.size()]));

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

            final ArrayList<DataExportSet> details = (ArrayList) getDetails(hit.getEntityId(), facets);
            for (DataExportSet detail : details) {
                row.add(detail.value);
            }
            try {
                csvWriter.write(row, processors);
            } catch (Exception e) {
                e.getMessage();
            }


        }

        csvWriter.close();
    }
}

