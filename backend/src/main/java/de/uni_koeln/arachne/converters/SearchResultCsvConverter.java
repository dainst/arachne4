package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.Transl8Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import java.util.List;

import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

import org.json.*;

/**
 * @author Sebastian Cuy
 * @author Patrick Jominet
 */
public class SearchResultCsvConverter extends AbstractHttpMessageConverter<SearchResult> {

    public SearchResultCsvConverter() {
        super(new MediaType("text", "csv"));
    }

    // because we can notz use @Autowired here
    private transient EntityService entityService;
    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }
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
        throw new UnsupportedOperationException("Reading CSV is not implemented yet and will most likely never be.");
    }

    @Override
    protected void writeInternal(SearchResult searchResult, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");

        final List<SearchResultFacet> facets = searchResult.getFacets();
        final List<SearchHit> entities = searchResult.getEntities();

        final List<String> headers = createHeaders(facets);

        final Writer writer = new OutputStreamWriter(httpOutputMessage.getBody());
        final CellProcessor[] processors = getProcessors(headers);
        final CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        csvWriter.writeHeader(headers.toArray(new String[headers.size()]));

        if( entities != null ){

            for (final SearchHit hit : entities) {

                final List<Object> row = new ArrayList<Object>();

                // there are items wo subtitle and maybe wo title out there...
                String title = (hit.getTitle() == null) ? "" : hit.getTitle().replace("\n", "").replace("\r", "");
                String subtitle = (hit.getSubtitle() == null) ? "" : hit.getSubtitle().replace("\n", "").replace("\r", "");

                row.add(String.valueOf(hit.getEntityId()) );
                row.add(hit.getType());
                row.add(title);
                row.add(subtitle);
                row.add(String.valueOf(hit.getThumbnailId()) );

                TypeWithHTTPStatus entity = null;

                if (entityService != null) {

                    try {
                        entity = entityService.getEntityFromIndex(hit.getEntityId(), null, "en");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (final SearchResultFacet facet : facets) {

                    if (entity != null) {
                        final String fullEntityString = entity.getValue().toString();

                        /*
                         * unpacking JSON here again might not be the most elegant solution but
                         * but writing an entire new function of getting data seemed to be nto very elegant
                         */
                        final JSONObject fullEntity = new JSONObject(fullEntityString);

                        if (fullEntity.has(facet.getName())) {
                            final Object valueObj = fullEntity.get(facet.getName());
                            if (valueObj instanceof JSONArray) {
                                if (facet.getName().equals("facet_geo")) {
                                    row.addAll(unpackFacetGeo((JSONArray) valueObj));
                                } else {
                                    row.add(((JSONArray) valueObj).get(0).toString());
                                }
                            } else {
                                row.add(valueObj.toString());
                            }
                        } else {
                            if (facet.getName().equals("facet_geo")) {
                                row.add("");
                                row.add("");
                                row.add("");
                            } else {
                                row.add(""); //"[none: " + facet.getName() + "]"
                            }

                        }
                    }


                }

                final List<Object> csvEntity = row;
                csvWriter.write(csvEntity, processors);
            }
        }

        csvWriter.close();
    }

    private List<String> createHeaders(List<SearchResultFacet> facets) {
        // table headers
        final List<String> headers = new ArrayList<String>();
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
                    headers.add(transl8Service.transl8(facet.getName(),"de"));
                } catch (Transl8Service.Transl8Exception e) {
                    headers.add(facet.getName());
                }
            }
        }
        return headers;
    }

    private ArrayList<String> unpackFacetGeo(JSONArray facetGeo) {

        final ArrayList returner = new ArrayList<String>(3);

        String gaz = "";
        String lat = "";
        String lon = "";
        // name is not necessary, since we have the columns land and place etc.

        // take first location
        Object firstEntryBox = facetGeo.get(0);
        final JSONObject firstEntry = new JSONObject("" + firstEntryBox); // dont't you remove the "" + -, it won't work then and I have no idea


        if (firstEntry.has("gazetteerId")) {
            gaz = firstEntry.get("gazetteerId").toString();
        }

        if (firstEntry.has("location")) {
            final JSONObject location = (JSONObject) firstEntry.get("location");
            lat = location.get("lat").toString();
            lon = location.get("lon").toString();
        }

        returner.add(gaz);
        returner.add(lat);
        returner.add(lon);

        return returner;
    }

    private static CellProcessor[] getProcessors(List<String> headers) {

        final CellProcessor[] cellProcessor = new StringCellProcessor[headers.size()];



        for(int i=0; i < headers.size(); i++){
            cellProcessor[i] = new Optional();
        }

        return cellProcessor;
    }
}
