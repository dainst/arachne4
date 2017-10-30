package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

import org.json.*;

/**
 * @author Sebastian Cuy
 * @author Patrick Jominet
 * @author Paf
 */
public class SearchResultCsvConverter extends DataExportConverter {

    public SearchResultCsvConverter() {
        super(new MediaType("text", "csv"));
    }


    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");

        final List<SearchResultFacet> facets = searchResult.getFacets(); //for (final SearchResultFacet facet : facets) {//facet.getName()
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

            final ArrayList<DataExportSet> details = (ArrayList) getDetails(hit, facets);
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

    private List<String> createHeaders(List<SearchResultFacet> facets) {
        // table headers
        final List<String> headers = new ArrayList<String>();
        headers.add("entityId");
        headers.add("type");
        headers.add("title");
        headers.add("subtitle");
        headers.add("thumbnailId");
        for (final SearchResultFacet facet : facets) {
            if (facet.getName().equals("facet_geo")) {
                headers.add("gazetteerId");
                headers.add("latitude");
                headers.add("longitude");
            } else {
                try {
                    headers.add(transl8Service.transl8(facet.getName(),"en"));
                } catch (Transl8Service.Transl8Exception e) {
                    headers.add(facet.getName());
                }
            }
        }
        return headers;
    }



    private static CellProcessor[] getProcessors(List<String> headers) {

        final CellProcessor[] cellProcessor = new StringCellProcessor[headers.size()];

        for(int i=0; i < headers.size(); i++){
            cellProcessor[i] = new Optional();
        }

        return cellProcessor;
    }

    @Override
    void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector) {
        collector.add(new DataExportSet("","", gazetteerId));
        collector.add(new DataExportSet("","", lat));
        collector.add(new DataExportSet("","", lon));
    }
}
