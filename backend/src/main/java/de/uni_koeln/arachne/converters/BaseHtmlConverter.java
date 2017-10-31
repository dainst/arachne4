package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONArray;
import org.springframework.http.*;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.service.Transl8Service;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;



/**
 * @author Paf
 */
public abstract class BaseHtmlConverter<T> extends AbstractDataExportConverter<T> {

    public BaseHtmlConverter() {
        super(MediaType.TEXT_HTML);
    }

    final String GAZETTEER_URL = "https://gazetteer.dainst.org/app/#!/show/%%%GAZID%%%";
    final String ALTERNATE_GEO_URL = "https://www.openstreetmap.org/?mlat=%%%LAT%%%&mlon=%%%LON%%%&zoom=15";

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

    public void htmlResults(List<SearchHit> entities, List<SearchResultFacet> facets) {
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

        final String facetString = ((longest > 14) && (facetValues.length() > 1))
            ? "<ul><li>" + String.join("</li><li>", values) + "</li></ul>"
            : String.join(", ", values);

        collector.add(new DataExportSet(facetName, facetFullName, facetString));
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
            final String line = "<img class='thumbnail' src='data:image/jpeg;base64," +  Base64.encode(imageBytes) + "' alt='thumbnail' ></img>";
            writer.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readImage(String imageName) {

        try {
            InputStream inputStream = servletContext.getResourceAsStream("WEB-INF/dataexport/" + imageName);
            Integer size = inputStream.available();
            byte[] filecontent = new byte[size];
            inputStream.read(filecontent,0, size);
            return filecontent;
        } catch (IOException e) {
            e.getMessage();
            //e.printStackTrace();
            return new byte[0];
        }

    }

    public void htmlFrontmatter(List<SearchResultFacet> facets) {

        // create replacements map
        HashMap<String, String> replacements = new HashMap<String, String>();

        // logo svg
        replacements.put("LOGOIMAGEDATA", "data:image/png;base64," + Base64.encode(readImage("dailogo.png")));

        // date
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        replacements.put("DATE", dateFormat.format(date));

        // facets
        String facetName = new String();
        ArrayList<String> facetNames = new ArrayList<String>();
        for (final SearchResultFacet facet : facets) {
            facetName = facet.getName();
            try {
                facetNames.add(transl8Service.transl8(facetName,"de"));
            } catch (Transl8Service.Transl8Exception e) {
                facetNames.add(facet.getName());
            }
        }
        replacements.put("FACETS", Arrays.toString(facetNames.toArray()));

        // search url
        replacements.put("SEARCHURL", getCurrentUrl().replace("&", "&amp;").replace("search.pdf", "search"));//

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
