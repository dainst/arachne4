package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExportTask {

    public UUID uuid = UUID.randomUUID();
    private DataExportConversionObject conversionObject;
    public AbstractDataExportConverter converter;
    private Timestamp tsCreated;
    private Timestamp tsStarted;
    private Timestamp tsStopped;
    private MediaType mediaType;
    private User owner;
    private String url = "";
    public Boolean error = false;

    private transient UserRightsService userRightsService;
    private String language;

    public DataExportTask(AbstractDataExportConverter converter,
                          DataExportConversionObject conversionObject) {
        this.converter = converter;
        this.conversionObject = conversionObject;
        this.tsCreated = new Timestamp(System.currentTimeMillis());
        setMediaType();
    }

    public void setUserRightsService(UserRightsService userRightsService) {
        this.userRightsService = userRightsService;
    }

    public UserRightsService getUserRightsService() {
        return userRightsService;
    }

    public DataExportConversionObject getConversionObject() {
        return conversionObject;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    private void setMediaType() {
        final List<MediaType> mediaTypes = converter.getSupportedMediaTypes();
        mediaType = mediaTypes.get(0);
    }

    public long getDuration() {
        if (tsStopped == null) {
            return new Timestamp(System.currentTimeMillis()).getTime() - tsStarted.getTime();
        } else {
            return tsStopped.getTime() - tsStarted.getTime();
        }
    }

    public long getAge() {
        return new Timestamp(System.currentTimeMillis()).getTime() - tsStarted.getTime();
    }

    public Timestamp getStartedTimeStamp() {
        return tsStarted;
    }

    public Timestamp getStoppedTimeStamp() {
        return tsStopped;
    }

    public Timestamp getCreatedTimeStamp() {
        return tsCreated;
    }

    public void startTimer() {
        this.tsStarted = new Timestamp(System.currentTimeMillis());
        this.tsStopped = null;
    }

    public void stopTimer() {
        this.tsStopped = new Timestamp(System.currentTimeMillis());
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject getInfoAsJSON() {
        final JSONObject info = new JSONObject();
        info.put("name", getConversionName());
        info.put("url", getUrl());
        info.put("mediaType", getMediaType().toString());
        info.put("conversionType", conversionObject.getType());
        info.put("owner", getOwner().getUsername());
        info.put("created_at", tsCreated.toString());
        if (tsStarted != null) {
            info.put("started_at", tsStarted.toString());
            info.put("duration", getDuration());
        }
        if (tsStopped != null) {
            info.put("stopped_at", tsStopped.toString());
        }
        return info;
    }

    public String getConversionName() {
       if (conversionObject.getType().equals("catalog")) {
            return converter.getConversionName(conversionObject.getCatalog());

       }
       if (conversionObject.getType().equals("searchResult")) {
            return converter.getConversionName(conversionObject.getSearchResult());
       }
       return "";
    }

    public void perform(OutputStream outputStream) throws IOException {
        userRightsService.setDataExporter(owner);
        converter.task = this;
        converter.convert(conversionObject, outputStream);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
