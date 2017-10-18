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
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;


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
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");

        final String[] headers = {"entityId", "type", "title", "subtitle", "thumbnailId"};
        final Writer writer = new OutputStreamWriter(httpOutputMessage.getBody());
        final CellProcessor[] processors = getProcessors();

        final CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        csvWriter.writeHeader(headers);

        final List<SearchHit> entities = searchResult.getEntities();
        if( entities != null ){
            for (final SearchHit hit : entities) {
                final List<Object> csvEntity = Arrays.asList(new Object[]{
                        String.valueOf(hit.getEntityId()),
                        hit.getType(),
                        hit.getTitle(),
                        hit.getSubtitle(),
                        String.valueOf(hit.getThumbnailId())
                });
                csvWriter.write(csvEntity, processors);
            }
        }

        csvWriter.close();
    }

    private static CellProcessor[] getProcessors() {
        return new CellProcessor[]{
                new Optional(), // entityId
                new Optional(), // type
                new Optional(), // title
                new Optional(), // subtitle
                new Optional()  // thumbnailId
        };
    }
}
