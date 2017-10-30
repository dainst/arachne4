package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.response.QuantificationContent;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.IIPService;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

import org.json.*;

/**
 * @author Paf
 */
public abstract class DataExportConverter extends AbstractHttpMessageConverter<SearchResult> {

    public DataExportConverter(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == SearchResult.class;
    }

    @Override
    protected SearchResult readInternal(Class<? extends SearchResult> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Reading other file formats is not implemented yet and will most likely never be.");
    }

    public Writer writer;

    public static final Logger LOGGER = LoggerFactory.getLogger(DataExportConverter.class);

    // because we can not use @Autowired (by any reason) here we have to inject nesessary classes like this
    public transient EntityService entityService;
    public transient Transl8Service transl8Service;
    public transient ServletContext servletContext;
    public transient IIPService iipService;
    public transient UserRightsService userRightsService;

    public void injectService(EntityService entityService) { this.entityService = entityService; }
    public void injectService(Transl8Service transl8Service) {
        this.transl8Service = transl8Service;
    }
    public void injectService(ServletContext servletContext) { this.servletContext = servletContext; }
    public void injectService(IIPService iipService) { this.iipService = iipService; }
    public void injectService(UserRightsService userRightsService) { this.userRightsService = userRightsService; }

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


    /**
     *
     * @param hit
     * @param facets
     * @return
     */
    public List<DataExportSet> getDetails(SearchHit hit, List<SearchResultFacet> facets) {

        final List<DataExportSet> row = new ArrayList<DataExportSet>(){};

        TypeWithHTTPStatus entity = null;

        try {
            entity = entityService.getEntityFromIndex(hit.getEntityId(), null, "en");
        } catch (Exception e) {
            //e.printStackTrace();  // LOG error
            return null;
        }
        if (entity == null) {
            return null;
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

            /*
             * unpacking JSON here again might not be the most elegant solution but
             * but writing an entire new function of getting data seemed to be not so very elegant either..
             */
            final JSONObject fullEntity = new JSONObject(entity.getValue().toString());

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
        /*
        returner.put("gazetteerId", gaz);
        returner.put("lat", lat);
        returner.put("lon", lon);
        */
    }

    abstract void handlePlace(Integer number, String name, String gazetteerId, String lat, String lon, String rel, List<DataExportSet> collector);

    void handleFacetValues(String facetName, String facetFullName, JSONArray facetValues, List<DataExportSet> collector) {
        for (int i = 0; i < facetValues.length(); i++) {
            collector.add(new DataExportSet(facetName, facetFullName, facetValues.get(i).toString()));
        }
    };

}