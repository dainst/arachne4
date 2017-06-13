package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * @author Sebastian Cuy
 * @author Patrick Jominet
 */
public class SearchResultCsvConverter extends AbstractHttpMessageConverter<SearchResult> {

    public SearchResultCsvConverter() {
        super(new MediaType("text", "csv"));
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    @Override
    protected SearchResult readInternal(Class<? extends SearchResult> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Reading CSV is not implemented yet and will most likely never be.");
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"searchResult.csv\"");

        // generate CSV content:
        final String[] headers = {"entityId", "type", "title", "subtitle", "thumbnailId"};
        final ArrayList<Object> entities = new ArrayList<>();
        for (final SearchHit hit : searchResult.getEntities()) {
            // create a generic object with only the values needed for CSV
            // should match number of fields defined in headers
            Object csvEntity = new Object[] {
                    String.valueOf(hit.getEntityId()),
                    hit.getType(),
                    hit.getTitle(),
                    hit.getSubtitle(),
                    hit.getThumbnailId()
            };
            entities.add(csvEntity);
        }

        // write CSV:
        try (final Writer writer = new OutputStreamWriter(httpOutputMessage.getBody())) {
            final CsvBeanWriter csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);

            csvWriter.writeHeader(headers);
            for (final Object entity : entities) {
                csvWriter.write(entity, headers);
            }
            csvWriter.close();
        }
    }
}
