package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.List;

public class SearchResult2CsvConverter extends BaseCsvConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        enqueIfHuge(searchResult, 200);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");
        convert(new DataExportConversionObject(searchResult), httpOutputMessage.getBody());
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final SearchResult searchResult = conversionObject.getSearchResult();
        this.writer = new OutputStreamWriter(outputStream);
        final List<SearchHit> entities = searchResult.getEntities();
        csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        initializeExport("Search Result"); // TODO transl8
        serialize(entities);
        csvHeaders();
        csvBody();
        csvFooter();
        csvWriter.close();
    }
}

