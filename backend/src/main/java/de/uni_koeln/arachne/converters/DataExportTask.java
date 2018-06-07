package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.response.search.SearchResult;
import org.jsoup.select.Evaluator;
import org.springframework.http.MediaType;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class DataExportTask {

    public String name = "";
    public UUID uuid = UUID.randomUUID();

    private Object conversionObject;
    private Class converterClass;
    private Timestamp timeStamp;
    private MediaType mediaType;

    public DataExportTask(String name, Class<? extends AbstractDataExportConverter> converterClass, Object conversionObject) {
        this.converterClass = converterClass;
        this.conversionObject = conversionObject;
        timeStamp = new Timestamp(System.currentTimeMillis());
        setMediaType();
        setName(name);
    }

    private void setMediaType() {
        try {
            final AbstractDataExportConverter converterInstance  = (AbstractDataExportConverter) converterClass.newInstance();
            final List<MediaType> mediaTypes = converterInstance.getSupportedMediaTypes();
            mediaType = mediaTypes.get(0);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void setName(String suffix) {
        name = converterClass.getName() + "_" + timeStamp.toString() + "_" + suffix;
    }
}
