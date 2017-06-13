package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchResult;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

/**
 * Created by scuy on 13.06.17.
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
        throw new IOException("Reading CSV is not implemented yet and will most likely never be.");
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getBody().write("yolö".getBytes("utf-8"));
    }
}
