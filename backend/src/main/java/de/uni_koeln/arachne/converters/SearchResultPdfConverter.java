package de.uni_koeln.arachne.converters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Paf
 */
public class SearchResultPdfConverter extends DataExportConverter {


    public SearchResultPdfConverter() {
        super(new MediaType("application", "pdf"));
    }

    // because we can not use @Autowired here
    private transient EntityService entityService;
    public void setEntityService(EntityService entityService) { this.entityService = entityService; }
    private transient Transl8Service transl8Service;
    public void setTransl8Service(Transl8Service transl8Service) {
        this.transl8Service = transl8Service;
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    @Override
    protected SearchResult readInternal(Class<? extends SearchResult> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Reading PDF. Dafuq u want from me?");
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.pdf\"");
    }


    public String getAsHtml() {
        return "!";
    }

    public byte[] getAsPdf(final Catalog catalog) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();

        pdfBuilder.withHtmlContent(getAsHtml(), "BASEURI");
        pdfBuilder.toStream(baos);
        try {
            pdfBuilder.run();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    @Override
    void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector) {

    }
}
