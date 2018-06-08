package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import org.json.JSONObject;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

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


    public DataExportTask(String url, User user,
                          AbstractDataExportConverter converter,
                          DataExportConversionObject conversionObject) {
        this.url = url;
        this.owner = user;
        this.converter = converter;
        this.conversionObject = conversionObject;
        this.tsCreated = new Timestamp(System.currentTimeMillis());
        setMediaType();
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

    public long getLifeTime() {
        if (tsStopped == null) {
            return new Timestamp(System.currentTimeMillis()).getTime() - tsStarted.getTime();
        } else {
            return tsStopped.getTime() - tsStarted.getTime();
        }
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

    public String getOwnerName() {
        return owner.getUsername();
    }

    public String getUrl() {
        return url;
    }

    public JSONObject getInfoAsJSON() {
        final JSONObject info = new JSONObject();
        info.put("url", getUrl());
        info.put("mediaType", getMediaType().toString());
        info.put("owner", getOwnerName());
        info.put("created_at", tsCreated.toString());
        if (tsStarted != null) {
            info.put("started_at", tsStarted.toString());
            info.put("duration", getLifeTime());
        }
        if (tsStopped != null) {
            info.put("stopped_at", tsStopped.toString());
        }
        return info;
    }

    public void perform(OutputStream outputStream) throws IOException {
        converter.convert(conversionObject, outputStream);
    }


}
