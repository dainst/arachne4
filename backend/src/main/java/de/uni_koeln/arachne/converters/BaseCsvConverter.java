package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.Transl8Service;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.io.CsvListWriter;

import java.io.IOException;
import java.util.*;


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


    public void csvHeaders() throws IOException {
        csvWriter.writeHeader(exportTable.headers.toArray(new String[exportTable.headers.size()]));
    }

    public void csvBody() throws IOException {

        DataExportCell cell;
        String value = "";

        for (final DataExportRow row : exportTable) {

            final ArrayList<String> fullRow = new ArrayList<String>(){};

            for (String header : exportTable.headers) {
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


    private List<String> getCsvHeaders(ArrayList<HashMap<String, DataExportCell>> csvTable) {
        final ArrayList<String> headers = new ArrayList<String>();



        return headers;
    }


    /**
     *
     * @param catalog
     * @return
     * @throws IOException
     */
    public void serialize(Catalog catalog) throws IOException {

        List<CatalogEntry> children = catalog.getRoot().getChildren();

        if (children != null) {
            Integer i = 1;
            for (CatalogEntry child : children) {
                serializeCatalogEntry(child, 0, i++ + "");
            }
        }


    }



    /**
     *
     */
    public void setProcessors() {
        csvProcessors = new StringCellProcessor[exportTable.headers.size()];
        for(int i=0; i < exportTable.headers.size(); i++){
            csvProcessors[i] = new Optional();
        }
    }

/*
    public List<String> mapDetails(ArrayList<DataExportCell> details) {
        List<String> result = new ArrayList<String>();

        for (DataExportCell set : details) {
            if (!set.isHeadline) {
                result.add(set.name);
            }
        }

        return result;

    }


    public List<String> facetList;
*/

    public void serializeCatalogEntry(final CatalogEntry catalogEntry, final int level, final String order) throws IOException {
        final DataExportRow row = exportTable.newRow();

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
        row.put("@order",    order);
        row.put("@id",       entityId.toString()); // id
        row.put("@title",    catalogEntry.getLabel()); // title
        row.putAll(getDetails(fullEntity));

        // children
        List<CatalogEntry> children = realGetChildren(catalogEntry);
        if (children != null) {
            Integer i = 1;
            for (CatalogEntry child : children) {
                serializeCatalogEntry(child, level + 1, order + "." + i++);
            }
        }

        exportTable.add(row);
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
    public void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {
        collector.put("place", gazetteerId);
        collector.put("lat", lat);
        collector.put("lon", lon);
    }
}
