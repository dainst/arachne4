package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.*;
import java.util.List;

public class SearchResult2PdfConverter extends BasePdfConverter<SearchResult> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException {
        enqueIfHuge(searchResult, 50);
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.pdf\"");
        convert(new DataExportConversionObject(searchResult), httpOutputMessage.getBody());
    }

    @Override
    public void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
        final SearchResult searchResult = conversionObject.getSearchResult();
        final List<SearchHit> entities = searchResult.getEntities();
        final List<SearchResultFacet> facets = searchResult.getFacets();
        BaseHtmlConverter htmlConverter = getHtmlConverter();
        htmlConverter.initializeExport(transl8("search_result_for") + " " + task.getConversionName());
        htmlConverter.writer = new StringWriter();
        htmlConverter.htmlHeader();
        htmlConverter.htmlFrontmatter();
        htmlConverter.htmlResults(entities, facets);
        htmlConverter.htmlFooter();
        writePdf((StringWriter) htmlConverter.writer, outputStream);
        htmlConverter.writer.close();
    }
}