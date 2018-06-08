package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.hibernate.User;
import org.json.JSONObject;
import org.springframework.http.MediaType;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class DataExportTask {

    public UUID uuid = UUID.randomUUID();
    private Object conversionObject;
    private Class converterClass;
    private Timestamp tsCreated;
    private Timestamp tsStarted;
    private Timestamp tsStopped;
    private MediaType mediaType;
    private User owner;
    private String url = "";


    public DataExportTask(String url, User user, Class<? extends AbstractDataExportConverter> converterClass, Object conversionObject) {
        this.converterClass = converterClass;
        this.conversionObject = conversionObject;
        this.owner = user;
        this.url = url;
        this.tsCreated = new Timestamp(System.currentTimeMillis());
        setMediaType();
    }

    private void setMediaType() {
        try {
            final AbstractDataExportConverter converterInstance  = (AbstractDataExportConverter) converterClass.newInstance();
            final List<MediaType> mediaTypes = converterInstance.getSupportedMediaTypes();
            mediaType = mediaTypes.get(0);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public String getConverterClassName() {
        return converterClass.getName();
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

    public MediaType getMediaType() {
        return mediaType;
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


}
