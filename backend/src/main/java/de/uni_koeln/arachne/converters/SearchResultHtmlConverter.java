package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.IIPService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import de.uni_koeln.arachne.util.security.JSONView;
import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONArray;
import org.springframework.http.*;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Paf
 */
public class SearchResultHtmlConverter extends DataExportConverter {

    final String GAZETTEER_URL = "https://gazetteer.dainst.org/app/#!/show/%%%GAZID%%%";
    final String ALTERNATE_GEO_URL = "https://www.openstreetmap.org/?mlat=%%%LAT%%%&mlon=%%%LON%%%&zoom=15";

    public SearchResultHtmlConverter() {
        super(new MediaType("text", "html"));
    }


    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/html");
        //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.html\"");

        final List<SearchHit> entities = searchResult.getEntities();
        final List<SearchResultFacet> facets = searchResult.getFacets();

        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        htmlHeader();
        htmlFrontmatter(searchResult); // @ TODO give facets instead
        writeResult(entities, facets);
        htmlFooter();
        writer.close();
    }

    private HashMap<String, String> htmlFileCache = new HashMap<String, String>();

    private String readHtmlFile(String file, HashMap<String, String> replacements) {

        if (htmlFileCache.containsKey(file)) {
            return replaceList(htmlFileCache.get(file), replacements);
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

        //htmlFileCache.put(file, fileContents.toString());

        return replaceList(fileContents.toString(), replacements);
    }
    private String readHtmlFile(String file) {
        return readHtmlFile(file, new HashMap<String, String>());
    }

    private String replaceList(String subject, HashMap<String, String> replacements) {
        for(HashMap.Entry<String, String> entry : replacements.entrySet()) {
            String code = entry.getKey();
            String repl = entry.getValue();
            subject = subject.replace("%%%" + code + "%%%", repl);
        }
        return subject;
    }

    private void writeResult(List<SearchHit> entities, List<SearchResultFacet> facets) {
        if(entities == null) {
            return;
        }
        try {
            for (final SearchHit hit : entities) {

                // there are items wo subtitle and maybe wo title out there...
                String title = (hit.getTitle() == null) ? "" : hit.getTitle();
                String subtitle = (hit.getSubtitle() == null) ? "" : hit.getSubtitle();

                writer.append("<div class='page'>");

                writer.append("<div class='page-left category infobox'>" + hit.getType() + "</div>");
                writer.append("<div class='page-right uri infobox'><a href='" + hit.getUri() + "' target='_blank'>" + hit.getUri() + "</a></div>");

                writer.append("<div class='row'>");
                htmlThumbnail(hit.getThumbnailId());
                writer.append("<h2 class='title'>" + title + "</h2>");
                writer.append("<h3 class='subtitle'>" + subtitle + "</h3>");


                writer.append("<table class='dataset'>");
                final ArrayList<DataExportSet> details = (ArrayList) getDetails(hit, facets);
                for (DataExportSet detail : details) {
                    if (detail != null) {
                        writer.append("<tr><td>" + detail.name + "</td><td>" + detail.value + "</td></tr>");
                    } else {
                        writer.append("<tr><td colspan='2'>error</td></tr>");
                    }
                }
                writer.append("</table>");

                /*@ TODO Stand jetzt
                 * # angepasste agg_geo handler schreiben je nach export format
                 * * merge multifecets
                 * * und dann den csv exporter reparieren,
                 * * dann standart auf JSON stellen
                 * * pdf
                 */


                writer.append("</div>");

                writer.append("</div>");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector) {
        final String fname = (rel.equals("")) ? "Place " + number.toString() : rel;
        number += 1;
        final String index = "place_" + number.toString();
        String value = new String();

        if (name.equals("") && !lat.equals("") && !lon.equals("")) {
            name = "[" + lat + "," + lon + "]";
        }

        if (!gazetteerId.equals("")) {
            value = "<a href='" + GAZETTEER_URL.replace("%%%GAZID%%%", gazetteerId) + "' target='_blank'>" + name + "</a>";
        } else if (!lat.equals("") && lon.equals("")) {
            value = "<a href='" + ALTERNATE_GEO_URL.replace("%%%LAT%%%", lat).replace("%%%LON%%%", lon) + "' target='_blank'>" + name + "</a>";
        } else {
            value = name;
        }

        collector.add(new DataExportSet(index, fname, value));
    }

    void handleFacetValues(String facetName, String facetFullName, JSONArray facetValues, List<DataExportSet> collector) {

        ArrayList<String> values = new ArrayList<String>();
        Integer longest = 0;

        for (int i = 0; i < facetValues.length(); i++) {
            values.add(facetValues.getString(i));
            longest = Math.max(longest, facetValues.getString(i).length());
        }

        collector.add(new DataExportSet(facetName, facetFullName, String.join((longest > 14 ? "<br>" : ", "), values)));
    }


    private void htmlThumbnail(Long entityId) {

        if (entityId == null) {
            return;
        }

        byte[] imageBytes = new byte[]{};

        final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_THUMBNAIL(), iipService.resolution_THUMBNAIL());
        if (image == null) {
            return;
        }
        imageBytes = image.getValue();
        if (imageBytes == null) {
            return;
        }

        try {
            writer.append("<img class='thumbnail' src='data:image/jpeg;base64," +  Base64.encode(imageBytes) + "'>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void htmlFrontmatter(SearchResult searchResult) {

        InputStream initFileStream = servletContext.getResourceAsStream("WEB-INF/dataexport/dailogo.svg");
        BufferedReader reader = new BufferedReader(new InputStreamReader(initFileStream));
        StringBuffer fileContents = new StringBuffer();
        try {
            while(reader.ready()){
                fileContents.append(reader.readLine());
            }
        } catch (IOException e) {
            e.getMessage();
            //e.printStackTrace();
        }

        // create replacements map
        HashMap<String, String> replacements = new HashMap<String, String>();

        // logo svg
        replacements.put("LOGOIMAGEDATA", "data:image/svg+xml;base64," + Base64.encode(String.valueOf(fileContents).getBytes()));

        // date
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        replacements.put("DATE", dateFormat.format(date));

        // facets
        String facetName = new String();
        ArrayList<String> facetNames = new ArrayList<String>();
        for (final SearchResultFacet facet : searchResult.getFacets()) {
            facetName = facet.getName();
            try {
                facetNames.add(transl8Service.transl8(facetName,"de"));
            } catch (Transl8Service.Transl8Exception e) {
                facetNames.add(facet.getName());
            }
        }
        replacements.put("FACETS", Arrays.toString(facetNames.toArray()));

        // search url
        replacements.put("SEARCHURL", getCurrentUrl());

        // user
        replacements.put("USER", getCurrentUser());



        try {
            writer.append(readHtmlFile("dataexport_frontmatter.html", replacements));
        } catch (IOException e) {
            e.getMessage();
            //e.printStackTrace();
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
            writer.append(readHtmlFile("dataexport_imprint.html"));
            writer.append(readHtmlFile("dataexport_footer.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
