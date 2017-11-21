package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.*;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import org.json.*;

/**
 * @author Paf
 */
public abstract class AbstractDataExportConverter<T> extends AbstractHttpMessageConverter<T> {

    public AbstractDataExportConverter(MediaType mediaType) {
        super(mediaType);
    }
    public AbstractDataExportConverter(MediaType... mediaTypes) {
        super(mediaTypes);
    }



    @Override
    protected T readInternal(Class<? extends T> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Reading other file formats is not implemented yet and will most likely never be.");
    }
/*
    @Override
    protected void writeInternal(T aClass, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException("This Endpoint does not support different output formats");
    }
*/

    public Writer writer;

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataExportConverter.class);

    // because we can not use @Autowired (by any reason) here, we have this fuck shit dependency injection here. plz don't hate me.
    public transient EntityService entityService;
    public transient Transl8Service transl8Service;
    public transient ServletContext servletContext;
    public transient IIPService iipService;
    public transient UserRightsService userRightsService;
    public transient CatalogEntryDao catalogEntryDao;
    public transient SingleEntityDataService singleEntityDataService;
    public transient EntityIdentificationService entityIdentificationService;

    public void injectService(EntityService entityService) { this.entityService = entityService; }
    public void injectService(Transl8Service transl8Service) {
        this.transl8Service = transl8Service;
    }
    public void injectService(ServletContext servletContext) { this.servletContext = servletContext; }
    public void injectService(IIPService iipService) { this.iipService = iipService; }
    public void injectService(UserRightsService userRightsService) { this.userRightsService = userRightsService; }
    public void injectService(CatalogEntryDao catalogEntryDao) { this.catalogEntryDao = catalogEntryDao; }
    public void injectService(SingleEntityDataService singleEntityDataService) { this.singleEntityDataService = singleEntityDataService; }
    public void injectService(EntityIdentificationService entityIdentificationService) { this.entityIdentificationService = entityIdentificationService; }

    // settings; overwrite em
    public Boolean includeEmptyFacets = false;
    public Boolean handleOnlyFirstPlace = false;
    public Boolean sortFacets = true;
    public List<String> skipFacets = Arrays.asList("facet_land", "facet_ort", "facet_ortsangabe");

    public String getCurrentUrl() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        URIBuilder ub = null;
        return request.getRequestURL().toString() + "?" + request.getQueryString();

    }

    public String getCurrentUser() {
        return userRightsService.getCurrentUser().getUsername();
    }

    // unpacks JSON and get all the objects datails against a list of facets


    public JSONObject getEntity(long entityId) {

        TypeWithHTTPStatus entity = null;

        try {
            entity = entityService.getEntityFromIndex(entityId, null, "en");
        } catch (Exception e) {
            //e.printStackTrace();  // LOG error
            return null;
        }
        if (entity == null) {
            return null;
        }

        final JSONObject fullEntity = new JSONObject(entity.getValue().toString());

        return fullEntity;
    }

    private void getSectionValueFromJson(List<DataExportSet> row, JSONObject box) {
       getSectionValueFromJson(row, box, "");
    }

    private void getSectionValueFromJson(List<DataExportSet> row, JSONObject box, String topLabel) {

        /**
         *
         *
         *
         * stand : wir können die sectiosn auslesen
         *
         * next: Überschriften Zeilen vernünftig markeiren (statt mit ##) und entsprechend auszeichnen
         *
         * Tickets:
         * - sections auslesen
         * - ui: partiell download / balken
         * - eingeloggt testen
         * - timeout
         *
         *
         */

        String label = box.has("label") ? box.get("label").toString() : null;
        Object boxValue = box.has("value") ? box.get("value") : null;

        label = (Objects.equals(label, "null")) ? null : label;

        if ((label != null) && (topLabel != null)) {
            row.add(new DataExportSet("","", topLabel, true));
        }

        if ((label == null) && (topLabel != null)) {
            label = topLabel;
        }

        if (boxValue instanceof JSONArray) {
            handleFacetValues(label, label, box.getJSONArray("value"), row);
        } else if (boxValue != null) {
            row.add(new DataExportSet(label, label, boxValue.toString()));
        }

        Object boxContent = box.has("content") ? box.get("content") : null;

        if (boxContent instanceof JSONObject) {
            getSectionValueFromJson(row, (JSONObject) boxContent, label);
        } else if (boxContent instanceof JSONArray) {
            for (int ii = 0; ii < ((JSONArray) boxContent).length(); ii++) {
                getSectionValueFromJson(row, ((JSONArray) boxContent).getJSONObject(ii), ii == 0 ? label : null);
            }
        }
    }


    /**
     *
     * @param entityId
     * @return
     */
    public List<DataExportSet> getDetails(long entityId) {

        final List<DataExportSet> row = new ArrayList<DataExportSet>() {};
        final JSONObject fullEntity = getEntity(entityId);

        if (fullEntity == null) {
            return row;
        }

        if (fullEntity.has("subtitle")) {
            row.add(new DataExportSet("xxx", "xxx", fullEntity.getString("subtitle"), true));
        }

        if (!fullEntity.has("sections")) {
            return row;
        }

        final JSONArray sections = fullEntity.getJSONArray("sections");

        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.getJSONObject(i);
            getSectionValueFromJson(row, section);
        }
        return row;
    }


    /**
     *
     * @param entityId
     * @param facets
     * @return List<DataExportSet>
     */
    public List<DataExportSet> getDetails(long entityId, List<SearchResultFacet> facets) {

        final List<DataExportSet> row = new ArrayList<DataExportSet>() {};
        final JSONObject fullEntity = getEntity(entityId);

        if (fullEntity == null) {
            return row;
        }


        for (final SearchResultFacet facet : facets) {

            final String facetName = facet.getName();

            if (skipFacets.contains(facetName)) {
                continue;
            }

            String facetFullName;
            try {
                facetFullName = transl8Service.transl8(facet.getName(), "en");
            } catch (Transl8Service.Transl8Exception e) {
                facetFullName = facetName;
            }

            if (!includeEmptyFacets && (!fullEntity.has(facet.getName()))) {
                continue;
            }

            final Object valueObj = (fullEntity.has(facet.getName())) ? fullEntity.get(facet.getName()) : "";

            if (valueObj instanceof JSONArray) {
                if (facetName.equals("facet_geo")) {
                    //row.putAll(unpackFacetGeo((JSONArray) valueObj));
                    if (fullEntity.has("places")) {
                        handlePlaces((JSONArray) fullEntity.get("places"), row);
                    } else if (fullEntity.has("facet_geo")) {
                        // some entities (iE http://bogusman02.dai-cloud.uni-koeln.de/data/entity/1179020) has a facet_geo, but no places
                        handlePlaces(unpackFacetGeo((JSONArray) fullEntity.get("facet_geo")), row);
                    }
                } else {
                    handleFacetValues(facetName, facetFullName, (JSONArray) valueObj, row);
                }

            } else { // fallback
                row.add(new DataExportSet(facetName, facetFullName, valueObj.toString()));
            }


        }

        // sort by index
        if (sortFacets) {
            Collections.sort(row, new Comparator<DataExportSet>() {
                public int compare(final DataExportSet object1, final DataExportSet object2) {
                    return object1.index.compareTo(object2.index);
                }
            });
        }

        return row;
    }

    public JSONArray unpackFacetGeo(JSONArray facetGeo) {
        for (int i = 0; i < (handleOnlyFirstPlace ? 1 : facetGeo.length()); i++) {
            Object entryBox = facetGeo.get(i);
            facetGeo.put(i, new JSONObject("" + entryBox)); // don't you remove the "" + -, it won't work then and I have no idea
        }
        return facetGeo;
    }

    public void handlePlaces(JSONArray places, List<DataExportSet> collector) {

        for (int i = 0; i < (handleOnlyFirstPlace ? 1 : places.length()); i++) {

            String gaz = "";
            String lat = "";
            String lon = "";
            String name= "";
            String rel = "";

            final JSONObject entry = places.getJSONObject(i);

            if (entry.has("name")) {
                name = entry.get("name").toString();
            }

            if (entry.has("gazetteerId")) {
                gaz = entry.get("gazetteerId").toString();
            }

            if (entry.has("relation")) {
                rel = entry.get("relation").toString();
            }

            if (entry.has("location")) {
                final JSONObject location = (JSONObject) entry.get("location");
                lat = location.get("lat").toString();
                lon = location.get("lon").toString();
            }

            handlePlace(i, name, gaz, lat, lon, rel, collector);

        }

    }

    abstract void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector);

    void handleFacetValues(String facetName, String facetFullName, JSONArray facetValues, List<DataExportSet> collector) {
        for (int i = 0; i < facetValues.length(); i++) {
            collector.add(new DataExportSet(facetName, facetFullName, facetValues.get(i).toString()));
        }
    };

}