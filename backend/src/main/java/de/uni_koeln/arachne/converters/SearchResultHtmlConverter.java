package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.IIPService;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.xerces.impl.dv.util.Base64;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

/**
 * @author Paf
 */
public class SearchResultHtmlConverter extends AbstractHttpMessageConverter<SearchResult> {


    public SearchResultHtmlConverter() {
        super(new MediaType("text", "html"));
    }

    // because we can not use @Autowired (by any reason) here we have to inject nesessary classes like this
    private transient EntityService entityService;
    public void setEntityService(EntityService entityService) { this.entityService = entityService; }

    private transient Transl8Service transl8Service;
    public void setTransl8Service(Transl8Service transl8Service) {
        this.transl8Service = transl8Service;
    }

    private transient ServletContext servletContext;
    public void setServletContext(ServletContext servletContext) { this.servletContext = servletContext; }

    private transient IIPService iipService;
    public void setIIPService(IIPService iipService) { this.iipService = iipService; }


    private Writer writer;

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
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/html");
        //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.html\"");

        final List<SearchHit> entities = searchResult.getEntities();
        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        htmlHeader();
        writeResult(entities);
        writer.close();
    }

    private HashMap<String, String> htmlFileCache = new HashMap<String, String>();

    private String readHtmlFile(String file) {

        if (htmlFileCache.containsKey(file)) {
            return htmlFileCache.get(file);
        }

        InputStream initFileStream = servletContext.getResourceAsStream("WEB-INF/dataexport/" + file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(initFileStream));
        StringBuffer fileContents = new StringBuffer();
        try {
            while(reader.ready()){
                fileContents.append(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        htmlFileCache.put(file, fileContents.toString());

        return fileContents.toString();
    }

    private void writeResult(List<SearchHit> entities) {
        if(entities != null) {
            try {
                for (final SearchHit hit : entities) {

                    final List<Object> row = new ArrayList<Object>();

                    // there are items wo subtitle and maybe wo title out there...
                    String title = (hit.getTitle() == null) ? "" : hit.getTitle();
                    String subtitle = (hit.getSubtitle() == null) ? "" : hit.getSubtitle();

                    writer.append("<div class='page'>");



                    writer.append("<div class='infobox row'>");
                    writer.append("<div class='category col-xs-6'>" + hit.getType() + "</div>");
                    writer.append("<div class='uri col-xs-6'><a href='" + hit.getUri() + "' target='_blank'>" + hit.getUri() + "</a></div>");
                    writer.append("</div>");

                    writer.append("<div class='row'>");

                    writer.append("<div class='col-md-6'>");
                    writer.append("<h2 class='title'>" + title + "</h2>");
                    writer.append("<h3 class='subtitle'>" + subtitle + "</h3>");
                    writer.append("</div>");

                    writer.append("<div class='thumbnailbox col-md-6'>");
                    htmlThumbnail(hit.getThumbnailId());
                    writer.append("</div>");

                    writer.append("</div>");


                    writer.append("</div>");


                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void htmlThumbnail(Long entityId) {

        if (entityId == null) {
            return;
        }

        final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_THUMBNAIL(), iipService.resolution_THUMBNAIL());
        final byte[] imageBytes = image.getValue();

        if (imageBytes == null) {
            return;
        }

        try {
            writer.append("<img src='data:image/jpeg;base64," +  Base64.encode(imageBytes) + "'>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void htmlHeader() {
        try {
            writer.append(readHtmlFile("dataexport_header.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void htmlFooter() {
        try {
            writer.append(readHtmlFile("dataexport_footer.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
