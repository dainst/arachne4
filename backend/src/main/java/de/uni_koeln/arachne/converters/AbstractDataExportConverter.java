package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    public DataExportTable exportTable = new DataExportTable();

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


    public JSONObject getEntity(long entityId) throws Exception {

        TypeWithHTTPStatus entity = null;

        try {
            entity = entityService.getEntityFromIndex(entityId, null, "en");
        } catch (Exception e) {
            //e.printStackTrace();  // LOG error
            return null;
        }
        if (!entity.getStatus().is2xxSuccessful()) {
            throw new Exception(entity.getStatus().toString());
        }
        if ((entity == null) || (!entity.getStatus().is2xxSuccessful())) {
            return null;
        }

        final JSONObject fullEntity = new JSONObject(entity.getValue().toString());

        return fullEntity;
    }

    /**
     * serialisation of section object
     * @param row
     * @param box
     */
    private void getSectionValueFromJson(DataExportRow row, JSONObject box) {
       getSectionValueFromJson(row, box, "", 0);
    }

    /**
     * serialisation of section object
     * @param row
     * @param box
     * @param topLabel
     */
    private void getSectionValueFromJson(DataExportRow row, JSONObject box, String topLabel, Integer number) {

        String label = box.has("label") ? box.get("label").toString() : null;
        Object boxValue = box.has("value") ? box.get("value") : null;

        label = (Objects.equals(label, "null")) ? null : label;

        if ((label == null) && (topLabel == null)) {
            return;
        }

        if (label == null) {
            label = topLabel;
        }

        if ((!label.equals(topLabel)) && (topLabel != null) && (number == 0)) {
            row.putHeadline(topLabel);
        }

        if (boxValue instanceof JSONArray) {
            serializeFacetValues(label, label, box.getJSONArray("value"), row);
        } else if (boxValue != null) {
            row.put(label, boxValue.toString());
        }

        Object boxContent = box.has("content") ? box.get("content") : null;

        if (boxContent instanceof JSONObject) {
            getSectionValueFromJson(row, (JSONObject) boxContent, label, 0);
        } else if (boxContent instanceof JSONArray) {
            for (int ii = 0; ii < ((JSONArray) boxContent).length(); ii++) {
                getSectionValueFromJson(row, ((JSONArray) boxContent).getJSONObject(ii), label, ii);
            }
        }
    }


    /**
     * serialized full Entity to
     * @param fullEntity
     * @return
     */
    public DataExportRow getDetails(JSONObject fullEntity) {

        final DataExportRow row = exportTable.newRow();

        if (fullEntity == null) {
            return row;
        }

        // subtitle
        if (fullEntity.has("subtitle")) {
            row.putHeadline("@subtitle", fullEntity.getString("subtitle"));
        }

        // sections
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
     * @return List<DataExportCell>
     */
    public DataExportRow getDetails(long entityId, List<SearchResultFacet> facets) {

        final DataExportRow row = exportTable.newRow();
        final JSONObject fullEntity;
        try {
            fullEntity = getEntity(entityId);
        } catch (Exception e) {
            LOGGER.error("Error retrieving entity " + entityId + ": " + e.getMessage());
            return row;
        }

        if (fullEntity == null) {
            return row;
        }


        for (final SearchResultFacet facet : facets) {

            final String facetName = facet.getName();

            if (skipFacets.contains(facetName)) {
                continue;
            }

            String facetFullName = null;
            try {
                facetFullName = transl8Service.transl8(facet.getName(), "en");
            } catch (Transl8Service.Transl8Exception e) {
                e.printStackTrace();
            }

            if (!includeEmptyFacets && (!fullEntity.has(facet.getName()))) {
                continue;
            }

            final Object valueObj = (fullEntity.has(facet.getName())) ? fullEntity.get(facet.getName()) : "";

            if (valueObj instanceof JSONArray) {
                if (facetName.equals("facet_geo")) {
                    serializePlaces(fullEntity, row);
                } else {
                    serializeFacetValues(facetName, facetFullName, (JSONArray) valueObj, row);
                }

            } else { // fallback
                row.put(facetName, facetFullName, valueObj.toString());
            }


        }

        return row;
    }

    void serializeFacetValues(String facetName, String facetFullName, JSONArray facetValues, DataExportRow collector) {
        for (int i = 0; i < facetValues.length(); i++) {
            collector.put(facetName, facetFullName, facetValues.get(i).toString());
        }
    };

    /**
     * extracts place information from fulLEntity and add it to collector - accorind to implementation if handlePlace function
     * @param fullEntity
     * @param collector
     */
    public void serializePlaces(JSONObject fullEntity, DataExportRow collector) {
        //row.putAll(unpackFacetGeo((JSONArray) valueObj));
        if (fullEntity.has("places")) {
            handlePlaces((JSONArray) fullEntity.get("places"), collector);
        } else if (fullEntity.has("facet_geo")) {
            // some entities (iE http://bogusman02.dai-cloud.uni-koeln.de/data/entity/1179020) has a facet_geo, but no places
            handlePlaces(unpackFacetGeo((JSONArray) fullEntity.get("facet_geo")), collector);
        }
    }

    /**
     * helper function to extract useful information about place
     *
     * @param places
     * @param collector
     */
    private void handlePlaces(JSONArray places, DataExportRow collector) {

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

    /**
     * helper function to extract useful information about place from facet_geo
     *
     * @param facetGeo
     * @return
     */
    private JSONArray unpackFacetGeo(JSONArray facetGeo) {
        for (int i = 0; i < (handleOnlyFirstPlace ? 1 : facetGeo.length()); i++) {
            Object entryBox = facetGeo.get(i);
            facetGeo.put(i, new JSONObject("" + entryBox)); // don't you remove the "" + -, it won't work then and I have no idea
        }
        return facetGeo;
    }

    /**
     *
     * implement this to define  how places shall get serilized!
     *
     * @param number
     * @param name
     * @param gazetteerId
     * @param lat
     * @param lon
     * @param rel
     * @param collector
     */

    abstract public void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector);


    public void initializeExport(String title) {
        exportTable = new DataExportTable();

        this.exportTable.title = title;
        this.exportTable.user = userRightsService.getCurrentUser().getUsername();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // @ TODO tansl8
        this.exportTable.timestamp = dateFormat.format(new Date());
    }

    public void initializeExport(Catalog catalog) {
        initializeExport(catalog.getRoot().getLabel());
        this.exportTable.author = catalog.getAuthor();
    }


}