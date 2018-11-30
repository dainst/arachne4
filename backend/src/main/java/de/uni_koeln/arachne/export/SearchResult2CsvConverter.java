package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class SearchResult2CsvConverter extends BaseCsvConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        enqueIfHuge(searchResult, 200);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");
        convert(new DataExportConversionObject(searchResult), httpOutputMessage.getBody());
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final SearchResult searchResult = conversionObject.getSearchResult();
        this.writer = new OutputStreamWriter(outputStream);
        final List<SearchHit> entities = searchResult.getEntities();
        csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        initializeExport(transl8("search_result"));
        serialize(entities);
        csvHeaders();
        csvBody();
        csvFooter();
        csvWriter.close();
    }
}

