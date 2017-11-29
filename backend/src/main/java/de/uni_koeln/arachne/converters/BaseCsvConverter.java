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
        final ArrayList<String> tableHeaders = exportTable.getColumns();
        csvWriter.writeHeader(tableHeaders.toArray(new String[tableHeaders.size()]));
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

    public void csvFooter() throws IOException {
        final ArrayList<String> row = new ArrayList<String>(){};
        row.add("");
        csvWriter.write(row);
        csvWriter.write(row);
        csvWriter.write(row);

        row.clear();
        row.add("Arachne Data Export");
        row.add(exportTable.title);
        csvWriter.write(row);

        if (exportTable.author != null) {
            row.clear();
            row.add("by");
            row.add(exportTable.author);
            csvWriter.write(row);
        }

        row.clear();
        row.add("Accessed at"); // TODO tranl8
        row.add(exportTable.timestamp);
        csvWriter.write(row);

        row.clear();
        row.add("by"); // TODO tranl8
        row.add(exportTable.user);
        csvWriter.write(row);

        row.clear();
        row.add("");
        csvWriter.write(row);

        row.clear();
        row.add("Imprint");
        row.add("https://arachne.dainst.org/info/imprint");
        csvWriter.write(row);

        row.clear();
        row.add("License");
        row.add("https://arachne.dainst.org/info/order");
        csvWriter.write(row);

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


    public void serializeCatalogEntry(final CatalogEntry catalogEntry, final int level, final String order) throws IOException {
        final DataExportRow row = exportTable.newRow();

        final Long entityId = catalogEntry.getArachneEntityId();

        // only export catalogue entries with connected entity
        /*if (entityId == null) {
            return;
        }*/

        String error = "";
        JSONObject fullEntity = null;
        try {
            fullEntity = (entityId != null) ? getEntity(entityId) : null;
        } catch (Exception e) {
            error = e.getMessage();
        }

        // serialize
        row.put("@@order", order);
        row.put("@@title", catalogEntry.getLabel());

        if (fullEntity != null) {
            row.put("@@id", entityId.toString());
            row.putAll(getDetails(fullEntity));
            serializePlaces(fullEntity, row);
        }
        if (!error.equals("")) {
            row.put("error", error);
        }

        exportTable.add(row);

        // children
        List<CatalogEntry> children = realGetChildren(catalogEntry);
        if (children != null) {
            Integer i = 1;
            for (CatalogEntry child : children) {
                serializeCatalogEntry(child, level + 1, order + "." + i++);
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





    @Override
    public void serializePlaces(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {
        collector.put("place", gazetteerId);
        collector.put("lat", lat);
        collector.put("lon", lon);
    }
}
