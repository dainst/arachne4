package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
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
import java.util.List;



/**
 * @author Sebastian Cuy
 * @author Patrick Jominet
 * @author Paf
 */
public abstract class BaseCsvConverter<T> extends AbstractDataExportConverter<T> {

    public BaseCsvConverter() {
        super(new MediaType("text", "csv"));
    }

    protected void writeInternal(Catalog catalog, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");
    }

    public List<String> createHeaders(List<SearchResultFacet> facets) {
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
                    headers.add(transl8Service.transl8(facet.getName(),"en")); // @ TODO set language
                } catch (Transl8Service.Transl8Exception e) {
                    headers.add(facet.getName());
                }
            }
        }
        return headers;
    }

    public static CellProcessor[] getProcessors(List<String> headers) {

        final CellProcessor[] cellProcessor = new StringCellProcessor[headers.size()];

        for(int i=0; i < headers.size(); i++){
            cellProcessor[i] = new Optional();
        }

        return cellProcessor;
    }

    @Override
    public void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector) {
        collector.add(new DataExportSet("","", gazetteerId));
        collector.add(new DataExportSet("","", lat));
        collector.add(new DataExportSet("","", lon));
    }
}
