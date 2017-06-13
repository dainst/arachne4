package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.Facet;
import de.uni_koeln.arachne.response.FacetList;
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
        //httpOutputMessage.getBody().write("yolo".getBytes("utf-8"));
        httpOutputMessage.getHeaders().set(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"searchResult.csv\"");

        try(final Writer writer = new OutputStreamWriter(httpOutputMessage.getBody())) {

            List<String> valuesList = new ArrayList<>();
            for (final SearchHit entity : searchResult.getEntities()) {
                valuesList.add(String.valueOf(entity.getEntityId()));
                valuesList.add(entity.getType());
                valuesList.add(entity.getTitle());
                valuesList.add(entity.getSubtitle());
                valuesList.add(String.valueOf(entity.getThumbnailId()));
            }
            final String[] values = new String[valuesList.size()];
            valuesList.toArray(values);

            final ICsvBeanWriter csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);

            final String[] headers = {"entityId", "type", "title", "subtitle", "thumbnailId"};
            csvWriter.writeHeader(headers);
            csvWriter.write(values, headers);

            csvWriter.close();
        }
    }
}
