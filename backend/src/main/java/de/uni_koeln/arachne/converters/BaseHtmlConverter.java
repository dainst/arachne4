package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.xerces.impl.dv.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.service.Transl8Service;

import java.io.*;
import java.util.*;



/**
 * @author Paf
 */
public abstract class BaseHtmlConverter<T> extends AbstractDataExportConverter<T> {

    public BaseHtmlConverter() {
        super(MediaType.TEXT_HTML);
    }

    public Integer maxCatalogDepth = 3;
    public Boolean getImages = true;
    //public Boolean validXml = true;

    final String GAZETTEER_URL = "https://gazetteer.dainst.org/app/#!/show/%%%GAZID%%%";
    final String ALTERNATE_GEO_URL = "https://www.openstreetmap.org/?mlat=%%%LAT%%%&mlon=%%%LON%%%&zoom=15";



    private HashMap<String, String> htmlFileCache = new HashMap<String, String>();

    private String readHtmlFile(String file, HashMap<String, String> replacements) throws IOException {

        if (htmlFileCache.containsKey(file)) {
            return replaceList(htmlFileCache.get(file), replacements);
        }

        InputStream initFileStream = servletContext.getResourceAsStream("WEB-INF/dataexport/" + file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(initFileStream));
        StringBuffer fileContents = new StringBuffer();
        while(reader.ready()){
            fileContents.append(reader.readLine());
        }


        //htmlFileCache.put(file, fileContents.toString());

        return replaceList(fileContents.toString(), replacements);
    }

    private String readHtmlFile(String file) throws IOException {
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

    public void htmlResults(List<SearchHit> entities, List<SearchResultFacet> facets) throws IOException {
        if(entities == null) {
            return;
        }

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

            htmlDetailTable(getDetails(hit.getEntityId(), facets));

            writer.append("</div>");

            writer.append("</div>");

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


    public void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {

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

    void serializeFacetValues(String facetName, String facetFullName, JSONArray facetValues, DataExportRow collector) {

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
            //e.printStackTrace();
            return new byte[0];
        }

    }

    public String facetList2String(List<SearchResultFacet> facets) {
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
        return Arrays.toString(facetNames.toArray());
    }


    /**
     * creates the first title page of the export
     * @param content
     * @throws IOException
     */
    public void htmlFrontmatter(String content) throws IOException {

        // search url
        final String url = getCurrentUrl()
            .replace(".pdf", "")
            .replace("?null", "")
            .replace(".html", "");


        // create replacements map
        HashMap<String, String> replacements = new HashMap<String, String>();

        // logo svg
        replacements.put("LOGOIMAGEDATA", "data:image/png;base64," + Base64.encode(readImage("dailogo.png")));

        writer.append("<div class='page frontmatter'>");
        writer.append("<div class='page-body'>");
        writer.append(readHtmlFile("dataexport_logo.html", replacements));

        writer.append("<div>");

        // serach results export front matter
        writer.append("<div class='doc-title'>");
        writer.append(exportTitle);
        if (exportAuthor != null) {
            writer.append("<span>");
            writer.append("by"); // TODO tranl8
            writer.append(" " + exportAuthor);
            writer.append("</span>");
        }
        writer.append("</div>");

        if (content != null) {
            writer.append("<p>");
            writer.append(content);
            writer.append("</p><br><br>");
        }

        writer.append("<p>");
        writer.append("Acceced at"); // TODO tranl8
        writer.append(" " + exportTimestamp + " ");
        writer.append("by"); // TODO tranl8
        writer.append(" " + exportUser);
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
        replacements.put("title", exportTitle);
        replacements.put("author", exportAuthor != null ? exportAuthor : exportUser);
        writer.append(readHtmlFile("dataexport_header.html", replacements));

        // printing layout footer
        writer.append("<div id='bottomLeftFooter'>");
        writer.append(exportTitle);
        writer.append(" | ");
        writer.append("Acceced at"); // TODO tranl8
        writer.append(" " + exportTimestamp + " ");
        writer.append("by"); // TODO tranl8
        writer.append(" " + exportUser);
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



    /**
     * Creates a HTML representation of a {@link CatalogEntry} and all its children.
     * @param catalogEntry The catalog entry.
     * @param level The recursion (indentation) level.
     * @return The entries as HTML.
     */
    private void htmlCatalogEntry(final CatalogEntry catalogEntry, final int level, final String headline) throws IOException {

        final Long entityId = catalogEntry.getArachneEntityId();
        final String idAsString = (entityId != null) ? entityId.toString() : "";
        final String uri = "https://arachne.dainst.org/entity/" + idAsString;
        final String label = catalogEntry.getLabel();
        final String text = catalogEntry.getText();
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
            error = (Objects.equals(error, "403")) ? ("User " + exportUser + " is not allowed to access this Dataset.") : ("Unknown Error: " + error); // TODO transl8
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

    private List<CatalogEntry> realGetChildren(CatalogEntry catalogEntry) { // @ TODO analyze why this is neccessary
        final List<CatalogEntry> storedChildren = catalogEntry.getChildren();
        if (storedChildren != null) {
            return storedChildren;
        }

        final User user = userRightsService.getCurrentUser();
        final CatalogEntry catalogEntry2 = catalogEntryDao.getById(catalogEntry.getId(), true, 5, 0);

        return catalogEntry2.getChildren();


    }




}
