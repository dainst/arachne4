package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.*;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paf
 *
 * Basic class for all converters to inherent
 *
 */

@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public abstract class AbstractDataExportConverter<T> extends AbstractHttpMessageConverter<T> {

    Writer writer;

    final Logger LOGGER = LoggerFactory.getLogger("DataExportLogger");

    transient EntityService entityService;
    transient Transl8Service transl8Service;
    transient ServletContext servletContext;
    transient IIPService iipService;
    transient CatalogEntryDao catalogEntryDao;
    private transient SingleEntityDataService singleEntityDataService;
    public transient EntityIdentificationService entityIdentificationService;
    private transient DataExportStack dataExportStack;

    User user;
    DataExportTable exportTable = new DataExportTable();
    DataExportTask task;

    // config

    final int totalMaximumForExport = 1000000;

    // settings; to overwrite in implementation if wanted

    final Boolean handleOnlyFirstPlace = false;
    final List<String> skipFacets = Arrays.asList("facet_land", "facet_ort", "facet_ortsangabe", "facet_image", "facet_geo", "facet_literatur");


    // constructors

    public AbstractDataExportConverter(MediaType mediaType) {
        super(mediaType);
    }

    public AbstractDataExportConverter(MediaType... mediaTypes) {
        super(mediaTypes);
    }

    // because we can not use @Autowired (by any reason) here, we have to use these shitty injection function here. plz don't hate me.

    public void injectService(EntityService entityService) {
        this.entityService = entityService;
    }

    public void injectService(Transl8Service transl8Service) {
        this.transl8Service = transl8Service;
    }

    public void injectService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void injectService(IIPService iipService) {
        this.iipService = iipService;
    }

    public void injectService(CatalogEntryDao catalogEntryDao) {
        this.catalogEntryDao = catalogEntryDao;
    }

    public void injectService(SingleEntityDataService singleEntityDataService) {
        this.singleEntityDataService = singleEntityDataService;
    }

    public void injectService(EntityIdentificationService entityIdentificationService) {
        this.entityIdentificationService = entityIdentificationService;
    }

    public void injectService(DataExportStack dataExportStack) {
        this.dataExportStack = dataExportStack;
    }

    protected T readInternal(Class<? extends T> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Reading other file formats is not implemented.");
    }


    // functions to be implemented

    abstract void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException;


    // conversion control functions

    void initializeExport(String title) {
        exportTable = new DataExportTable();
        this.exportTable.title = title;
        this.exportTable.user = task.getOwner().getUsername();
        String dateFormatString = transl8("date_format");
        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        this.exportTable.timestamp = dateFormat.format(new Date());
    }

    void initializeExport(Catalog catalog) {
        initializeExport(catalog.getRoot().getLabel());
        this.exportTable.author = catalog.getAuthor();
    }

    private void checkForHugeAndEnqueue(Long size, Integer limit, DataExportConversionObject conversionObject) {

        task = dataExportStack.newTask(this, conversionObject);

        if (size < limit) {
            return;
        }

        if (size > totalMaximumForExport) {
            throw new DataExportException("to_huge", HttpStatus.BAD_REQUEST);
        }

        dataExportStack.push(task);

        throw new DataExportException("to_huge_and_will_be_sent_by_mail", HttpStatus.ACCEPTED);
    }

    void enqueueIfHuge(SearchResult searchResult, Integer limit) {
        checkForHugeAndEnqueue(searchResult.getSize(), limit, new DataExportConversionObject(searchResult));
    }

    void enqueueIfHuge(Catalog catalog, Integer limit) {
        checkForHugeAndEnqueue((long) catalog.getRoot().getAllSuccessors(), limit, new DataExportConversionObject(catalog));
    }

    // helping functions, most converters would use

    // Unpacks JSON and get all the objects details against a list of facets
    protected JSONObject getEntity(long entityId) throws Exception {

        TypeWithHTTPStatus entity;

        try {
            entity = entityService.getEntityFromIndex(entityId, null, "en");
        } catch (Exception e) {
            LOGGER.error("Could not get Entity", e);
            return null;
        }
        if (!entity.getStatus().is2xxSuccessful()) {
            throw new Exception(entity.getStatus().toString());
        }
        if ((entity == null) || (!entity.getStatus().is2xxSuccessful())) {
            return null;
        }

        return new JSONObject(entity.getValue().toString());
    }

    // serialisation of section object
    private void serializeSection(DataExportRow row, JSONObject box) {
        serializeSection(row, box, "", 0);
    }

    private void serializeSection(DataExportRow row, JSONObject box, String topLabel, Integer number) {

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
            serializeSection(row, (JSONObject) boxContent, label, 0);
        } else if (boxContent instanceof JSONArray) {
            for (int ii = 0; ii < ((JSONArray) boxContent).length(); ii++) {
                serializeSection(row, ((JSONArray) boxContent).getJSONObject(ii), label, ii);
            }
        }
    }

    //serialized a complete Entity to
    public DataExportRow getDetails(JSONObject fullEntity) {

        final DataExportRow row = exportTable.newRow();

        if (fullEntity == null) {
            return row;
        }

        // subtitle
        if (fullEntity.has("subtitle")) {
            row.putHeadline("@subtitle", fullEntity.getString("subtitle"));
        }

        serializeFacets(row, fullEntity);

        // sections
        if (!fullEntity.has("sections")) {
            return row;
        }

        final JSONArray sections = fullEntity.getJSONArray("sections");

        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.getJSONObject(i);
            serializeSection(row, section);
        }

        return row;
    }

    // serlializes the facets of a given Fullentity (without knowing them beforehand)
    private void serializeFacets(DataExportRow row, JSONObject fullEntity) {
        Object value;
        String fullFacetName;
        for (String key : fullEntity.keySet()) {
            if (key.length() <= 6) {
                continue;
            }
            if (!key.substring(0, 6).equals("facet_")) {
                continue;
            }

            if (skipFacets.contains(key)) {
                continue;
            }

            value = fullEntity.get(key);

            if (!(value instanceof JSONArray)) {
                continue;
            }

            fullFacetName = transl8(key.substring(6));

            row.put("@" + fullFacetName, fullFacetName, (String) ((JSONArray) value).getString(0));
        }
    }

    protected void serializeFacetValues(String facetName, String facetFullName, JSONArray facetValues, DataExportRow collector) {
        for (int i = 0; i < facetValues.length(); i++) {
            collector.put(facetName, facetFullName, facetValues.get(i).toString());
        }
    };

    // extracts place information from fulLEntity and add it to collector - accorind to implementation if serializePlaces function
    protected void serializePlaces(JSONObject fullEntity, DataExportRow collector) {
        if (fullEntity.has("places")) {
            serializePlacesArray((JSONArray) fullEntity.get("places"), collector);
        } else if (fullEntity.has("facet_geo")) {
            // some entities (iE http://bogusman02.dai-cloud.uni-koeln.de/data/entity/1179020) has a facet_geo, but no places
            serializePlacesArray(unpackFacetGeo((JSONArray) fullEntity.get("facet_geo")), collector);
        }
    }

    // helper function to extract useful information about place
    protected void serializePlacesArray(JSONArray places, DataExportRow collector) {

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

            serializePlaces(i, name, gaz, lat, lon, rel, collector);

        }

    }

    // implement this to define how places shall get serialized!
    void serializePlaces(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {
    };

    // helper function to extract useful information about place from facet_geo
    private JSONArray unpackFacetGeo(JSONArray facetGeo) {
        for (int i = 0; i < (handleOnlyFirstPlace ? 1 : facetGeo.length()); i++) {
            Object entryBox = facetGeo.get(i);
            facetGeo.put(i, new JSONObject("" + entryBox)); // don't you remove the "" + -, it won't work then and I have no idea
        }
        return facetGeo;
    }

    public String getConversionName(Catalog catalog) {
        return catalog.getRoot().getLabel();
    }

    public String getConversionName(SearchResult searchResult) {
        try {

            final String delimiter = " " + transl8("and") + " ";
            final String regex = "facet_(\\w+):\\\"(.*)\\\"";
            final Pattern pattern = Pattern.compile(regex);
            final List<NameValuePair> params = URLEncodedUtils.parse(new URI(task.getRequestUrl()), "UTF-8");
            final ArrayList<String> queryFilers = new ArrayList<String>(){};
            for (NameValuePair param : params) {
                if (param.getName().equals("q")) {
                    queryFilers.add(0, "'" + param.getValue() + "'");
                }
                if (param.getName().equals("fq")) {
                    final Matcher matcher = pattern.matcher(param.getValue());
                    while (matcher.find()) {
                        queryFilers.add(matcher.group(1) + " = '" + matcher.group(2) + "'");
                    }
                }
            }

            return String.join(delimiter, queryFilers);

        } catch (Exception e) {
            return "";
        }
    }

    String transl8(String key) {
        try {
            return transl8Service.transl8(key, task.getLanguage());
        } catch (Exception e) {
            LOGGER.warn("could not transl8: " + key);
            return '#' + key;
        }
    }

    // because catalog endpoint is not necessarily called with full-parameter, children might be missing
    List<CatalogEntry> realGetChildren(CatalogEntry catalogEntry) {
        final List<CatalogEntry> storedChildren = catalogEntry.getChildren();
        if (storedChildren != null) {
            return storedChildren;
        }

        final CatalogEntry catalogEntry2 = catalogEntryDao.getById(catalogEntry.getId(), true, 5, 0);

        return catalogEntry2.getChildren();
    }

}