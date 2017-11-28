package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.Transl8Service;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;


/**
 *
 * @param <T>
 */
public abstract class BaseCsvConverter<T> extends AbstractDataExportConverter<T> {

    public BaseCsvConverter() {
        super(new MediaType("text", "csv"));
    }

    public CsvListWriter csvWriter;
    public CellProcessor[] csvProcessors;


    public void csvHeaders(TreeSet<String> headers) throws IOException {
        csvWriter.writeHeader(headers.toArray(new String[headers.size()]));
    }

    public void csvBody(TreeSet<String> headers, ArrayList<HashMap<String, DataExportSet>> table) throws IOException {

        DataExportSet cell;
        String value = "";

        for (final HashMap<String, DataExportSet> row : table) {

            final ArrayList<String> fullRow = new ArrayList<String>(){};

            for (String header : headers) {
                cell = row.get(header);
                value = (cell == null) ? "" : cell.value;
                fullRow.add(value);
            }

            csvWriter.write(fullRow);
        }

    }


    /**
     * tables headers for csv fpr SEARCH RESULTS
     * @param facets
     * @return
     */
    public TreeSet<String> getCsvHeaders(List<SearchResultFacet> facets) {

        final TreeSet<String> headers = new TreeSet<String>();
        headers.add("entityId");
        headers.add("type");
        headers.add("title");
        headers.add("subtitle");
        headers.add("thumbnailId");

        for (final SearchResultFacet facet : facets) {
            if (facet.getName().equals("facet_geo")) {
                headers.add("gazetteerId");
                headers.add("latitude");
                headers.add("longitude");
            } else {
                try {
                    headers.add(transl8Service.transl8(facet.getName(),"en")); // @ TODO set language
                } catch (Transl8Service.Transl8Exception e) {
                    headers.add(facet.getName());
                }
            }
        }
        return headers;
    }

    public TreeSet<String> getCsvHeaders(Catalog catalog) {
        final TreeSet<String> headers = new TreeSet<String>();
        headers.add("order");
        headers.add("entityId");
        headers.add("title");

        return headers;
    }


    private List<String> getCsvHeaders(ArrayList<HashMap<String, DataExportSet>> csvTable) {
        final ArrayList<String> headers = new ArrayList<String>();



        return headers;
    }


    /**
     *
     * @param catalog
     * @return
     * @throws IOException
     */
    public ArrayList<HashMap<String, DataExportSet>> getCsvTable(Catalog catalog) throws IOException {

        final ArrayList<HashMap<String, DataExportSet>> csvTable = new ArrayList<HashMap<String, DataExportSet>>();

        List<CatalogEntry> children = catalog.getRoot().getChildren();

        if (children != null) {
            Integer i = 1;
            for (CatalogEntry child : children) {
                csvCatalogEntry(child, 0, i++ + "", csvTable);
            }
        }

        return csvTable;

    }



    /**
     *
     * @param headers
     */
    public void setProcessors(TreeSet<String> headers) {
        csvProcessors = new StringCellProcessor[headers.size()];
        for(int i=0; i < headers.size(); i++){
            csvProcessors[i] = new Optional();
        }
    }

/*
    public List<String> mapDetails(ArrayList<DataExportSet> details) {
        List<String> result = new ArrayList<String>();

        for (DataExportSet set : details) {
            if (!set.isHeadline) {
                result.add(set.name);
            }
        }

        return result;

    }


    public List<String> facetList;
*/

    public void csvCatalogEntry(final CatalogEntry catalogEntry, final int level, final String order, final List<HashMap<String, DataExportSet>> table) throws IOException {
        final HashMap<String, DataExportSet> row = new HashMap<String, DataExportSet>();

        final Long entityId = catalogEntry.getArachneEntityId();

        // only export catalogue entries with connected entity
        if (entityId == null) {
            return;
        }

        String error = "";

        JSONObject fullEntity = new JSONObject();

        try {
            fullEntity = getEntity(entityId);
        } catch (Exception e) {
            error = e.getMessage();
        }



        // serialize
        row.put("order",    new DataExportSet("order", order));
        row.put("id",       new DataExportSet("id", entityId.toString())); // id
        row.put("title",    new DataExportSet("title", catalogEntry.getLabel())); // title
        row.putAll(getDetails(fullEntity));

        //row.addAll(details);
        /**
         * stand: wir müssen vorher die Zahl der Spalten kennen
         * # wir müssen es so ändern dass in der row die indices der spalten name ist!
         * - wir müssen erst alle Zeilen sammeln,
         * - dann gucken welche spalten es gibt und erst dann pronto
         *
         */


        // children
        List<CatalogEntry> children = realGetChildren(catalogEntry);
        if (children != null) {
            Integer i = 1;
            for (CatalogEntry child : children) {
                csvCatalogEntry(child, level + 1, order + "." + i++, table);
            }
        }

        table.add(row);
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



    @Override
    public void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, HashMap<String, DataExportSet> collector) {
        collector.put(getColumnName("place", collector), new DataExportSet("", gazetteerId));
        collector.put(getColumnName("lat", collector), new DataExportSet("", lat));
        collector.put(getColumnName("lon", collector), new DataExportSet("", lon));
    }
}
