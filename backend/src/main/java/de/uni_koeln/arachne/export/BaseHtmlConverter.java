package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.xerces.impl.dv.util.Base64;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Paf
 * 
 * @param <T> The type the converter handles.
 */

public abstract class BaseHtmlConverter<T> extends AbstractDataExportConverter<T> {

    public BaseHtmlConverter() {
        super(MediaType.TEXT_HTML);
    }

    public Boolean getImages = true;

    final String GAZETTEER_URL = "https://gazetteer.dainst.org/app/#!/show/%%%GAZID%%%";
    final String ALTERNATE_GEO_URL = "https://www.openstreetmap.org/?mlat=%%%LAT%%%&mlon=%%%LON%%%&zoom=15";

    private HashMap<String, String> htmlFileCache = new HashMap<String, String>();

    private String readHtmlFile(String file, HashMap<String, String> replacements) throws IOException {

        if (htmlFileCache.containsKey(file)) {
            return replaceList(htmlFileCache.get(file), replacements);
        }

        InputStream initFileStream = servletContext.getResourceAsStream("WEB-INF/dataexport/" + file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(initFileStream));
        StringBuilder fileContents = new StringBuilder();
        while(reader.ready()){
            fileContents.append(reader.readLine());
        }

        htmlFileCache.put(file, fileContents.toString());

        return replaceList(fileContents.toString(), replacements);
    }

    private String readHtmlFile(String file) throws IOException {
        return readHtmlFile(file, new HashMap<String, String>());
    }

    private String replaceList(String subject, HashMap<String, String> replacements) {
        for(HashMap.Entry<String, String> entry : replacements.entrySet()) {
            String code = entry.getKey();
            String repl = entry.getValue();
            if (repl != null) {
                subject = subject.replace("%%%" + code + "%%%", repl);
            }
        }
        return subject;
    }

    public void htmlResults(List<SearchHit> entities, List<SearchResultFacet> facets) throws IOException {
        if(entities == null) {
            return;
        }

        for (final SearchHit hit : entities) {

            // there are items wo subtitle and maybe wo title out there...
            String title = (hit.getTitle() == null) ? "" : hit.getTitle();
            
            writer.append("<div class='page'>");

            writer.append("<table class='page-header'><tr>");
            writer.append("<td class='page-header-left'>");
            writer.append(hit.getType());
            writer.append("</td>");
            writer.append("<td class='page-header-right'>");
            writer.append("<a href='" + hit.getUri() + "' target='_blank'>" + hit.getUri() + "</a>");
            writer.append("</td>");
            writer.append("</tr></table>"); // page-header

            writer.append("<div class='page-body'>");

            htmlThumbnail(hit.getThumbnailId());

            writer.append("<h1 class='title'>" + title + "</h1>");

            try {
                htmlDetailTable(getDetails(getEntity(hit.getEntityId())));
            } catch (Exception e) {
                String error = (Objects.equals(e.getMessage(), "403")) ? ("User " + exportTable.user + " is not allowed to access this Dataset.") : ("Unknown Error: " + e.getMessage());
                writer.append("<p class='error'>" + error + "</p>");
            }


            writer.append("</div>"); // page-body
            writer.append("<div class='page-footer'>");
            writer.append("</div>"); // page-footer
            writer.append("</div>"); // page

        }
    }


    public void htmlDetailTable(HashMap<String, DataExportCell> details) throws IOException {
        writer.append("<table class='dataset'>");
        for (String key : details.keySet()) {
            final DataExportCell detail = details.get(key);
            if (detail.isHeadline) {
                writer.append("<tr><td colspan='2' class='section'>" + detail.value + "</td></tr>");
            } else if (detail.value != null) {
                writer.append("<tr><td>" + detail.name + "</td><td>" + detail.value + "</td></tr>");
            } else if (detail.name.equals("") && detail.value.equals("")) {
                // do nothing
            } else {
                writer.append("<tr><td colspan='2' class='error'>Unknown Error</td></tr>");
            }
        }
        writer.append("</table>");
    }

    @Override
    protected void serializePlaces(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {

        String value;

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

        collector.put("place", value);
    }

    @Override
    protected void serializeFacetValues(String facetName, String facetFullName, JSONArray facetValues, DataExportRow collector) {

        ArrayList<String> values = new ArrayList<String>();
        Integer longest = 0;

        for (int i = 0; i < facetValues.length(); i++) {
            values.add(facetValues.getString(i));
            longest = Math.max(longest, facetValues.getString(i).length());
        }

        final String facetString = ((longest > 14) && (facetValues.length() > 1))
            ? "<ul><li>" + String.join("</li><li>", values) + "</li></ul>"
            : String.join(", ", values);

        collector.put(facetName, facetFullName, facetString);
    }


    private void htmlThumbnail(Long entityId) throws IOException {

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

        final String line = "<img class='thumbnail' src='data:image/jpeg;base64," +  Base64.encode(imageBytes) + "' alt='thumbnail' ></img>";
        writer.append(line);
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
            LOGGER.error("Could not get image", e);
            return new byte[0];
        }

    }

    public String facetList2String(List<SearchResultFacet> facets) {
        // facets
        String facetName;
        ArrayList<String> facetNames = new ArrayList<String>();
        for (final SearchResultFacet facet : facets) {
            facetName = facet.getName();
            try {
                facetNames.add(transl8Service.transl8(facetName,"de"));
            } catch (Transl8Service.Transl8Exception e) {
                LOGGER.warn("could not transl:" + facet.getName());
                facetNames.add(facet.getName());
            }
        }
        return Arrays.toString(facetNames.toArray());
    }


    /**
     * creates the first title page of the export
     * @param content - string fpr the FM-Text
     * @throws IOException - IOexception
     */
    public void htmlFrontmatter(String content) throws IOException {

        // search url
        final String url = task.getRequestUrl()
            .replace("http://arachnedataservice/data", task.getBackendUrl())
            .replace("/data", "")
            .replace("mediaType=pdf", "")
            .replace("mediaType=html", "")
            .replace("?null", "")
            .replace("?&", "?")
            .replace("&&", "&");

        // create replacements map
        HashMap<String, String> replacements = new HashMap<String, String>();

        // logo
        replacements.put("LOGOIMAGEDATA", "data:image/png;base64," + Base64.encode(readImage("dailogo.png")));

        writer.append("<div class='page frontmatter'>");
        writer.append("<div class='page-body'>");
        writer.append(readHtmlFile("dataexport_logo.html", replacements));

        writer.append("<div>");

        // search results export front matter
        writer.append("<div class='doc-title'>");
        writer.append(exportTable.title);
        if (exportTable.author != null) {
            writer.append("<span>");
            writer.append(transl8("by"));
            writer.append(" " + exportTable.author);
            writer.append("</span>");
        }
        writer.append("</div>");

        if (content != null) {
            writer.append("<p>");
            writer.append(content);
            writer.append("</p><br><br>");
        }

        writer.append("<p>");
        writer.append(transl8("accessed_at"));
        writer.append(" " + exportTable.timestamp + " ");
        writer.append(transl8("by"));
        writer.append(" " + exportTable.user);
        writer.append("</p>");
        writer.append("<p><a href='" + url + "'>" + url + "</a></p>");

        writer.append("</div>");

        writer.append("</div>"); //page-body

        writer.append("</div>"); //page
    }

    public void htmlFrontmatter() throws IOException {
        htmlFrontmatter(null);
    }

    public void htmlHeader() throws IOException {

        // html header & stuff
        HashMap<String, String> replacements = new HashMap<String, String>();
        replacements.put("title", exportTable.title);
        replacements.put("author", exportTable.author != null ? exportTable.author : exportTable.user);
        writer.append(readHtmlFile("dataexport_header.html", replacements));

        // printing layout footer
        writer.append("<div id='bottomLeftFooter'>");
        writer.append(exportTable.title);
        writer.append(" | ");
        writer.append(transl8("accessed_at"));
        writer.append(" " + exportTable.timestamp + " ");
        writer.append(transl8("by"));
        writer.append(" " + exportTable.user);
        writer.append("</div>");
    }

    public void htmlFooter() throws IOException {
        writer.append(readHtmlFile("dataexport_imprint.html"));
        writer.append(readHtmlFile("dataexport_footer.html"));
    }

    public void htmlCatalog(Catalog catalog) throws IOException {
        List<CatalogEntry> children = catalog.getRoot().getChildren();

        if (children != null) {
            for (CatalogEntry child : children) {
                htmlCatalogEntry(child, 0, "");
            }
        }
    }

    String markdown2html(String markdown) {
        if (markdown == null) {
            return "";
        }
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    /**
     * Creates a HTML representation of a {@link CatalogEntry} and all its children.
     * @param catalogEntry The catalog entry.
     * @param level The recursion (indentation) level.
     */
    private void htmlCatalogEntry(final CatalogEntry catalogEntry, final int level, final String headline) throws IOException {

        final Long entityId = catalogEntry.getArachneEntityId();
        final String idAsString = (entityId != null) ? entityId.toString() : "";
        final String uri = task.getBackendUrl() + "/entity/" + idAsString;
        final String label = catalogEntry.getLabel();
        final String text = markdown2html(catalogEntry.getText());
        final String headlineTag = "h" + Math.min(6, (level + 1));
        final String headClass = "level-" + level;
        final String count = catalogEntry.getTotalChildren() + "";
        final Boolean hasChildren = (!count.equals("")) && (!count.equals("0"));
        String error = null;

        // entity
        DataExportRow details = null;
        JSONObject fullEntity = new JSONObject();

        if (entityId != null) {
            try {
                fullEntity = getEntity(entityId);
            } catch (Exception e) {
                error = e.getMessage();
            }

            details = getDetails(fullEntity);
        }

        writer.append("<div class='page " + headClass + "'>");

        writer.append("<table class='page-header'><tr>");
        writer.append("<td class='page-header-left'>");
        if (!Objects.equals(headline, "")) {
            writer.append(headline);
        }
        writer.append("</td>");
        writer.append("<td class='page-header-right'>");
        if (!idAsString.equals("")) {
            writer.append("<a href='" + uri + "' target='_blank'>" + uri + "</a>");
        }
        writer.append("</td>");
        writer.append("</tr></table>"); // page-header


        writer.append("<div class='page-body'>");

        if ((fullEntity != null) && getImages) {
            if (fullEntity.has("images")) {
                final JSONArray images = fullEntity.getJSONArray("images");
                if (images.length() > 0) {
                    htmlThumbnail(images.getJSONObject(0).getLong("imageId"));
                }
            }
        }

        if (label != null) {
            writer.append("<" + headlineTag +" class='title'>" + label + "</" + headlineTag + ">");
        }

        if (text != null) {
            writer.append("<p>" + text + "</p>");
        }

        // entity
        if (error != null) {
            error = (Objects.equals(error, "403")) ? ("User " + exportTable.user + " is not allowed to access this Dataset.") : ("Unknown Error: " + error);
            writer.append("<p class='error'>" + error + "</p>");
        }

        if (details != null) {
            htmlDetailTable(details);
        }

        writer.append("</div>"); //page-body

        writer.append("<div class='page-footer'>");
        if (hasChildren) {
            writer.append("<span>" + count + " Entries</span>");
        }
        writer.append("</div>"); //page-footer

        writer.append("</div>"); //page

        // children
        List<CatalogEntry> children = realGetChildren(catalogEntry);
        if (children != null) {
            for (CatalogEntry child : children) {
                htmlCatalogEntry(child, level + 1, label);
            }
        }

    }

}
