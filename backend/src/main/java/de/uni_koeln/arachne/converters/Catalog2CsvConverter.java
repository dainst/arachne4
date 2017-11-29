package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;


public class Catalog2CsvConverter extends BaseCsvConverter<Catalog> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == Catalog.class;
    }

    @Override
    protected void writeInternal(Catalog catalog, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {

        httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/csv");
        //httpOutputMessage.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"currentSearch.csv\"");

        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);

        initializeExport(catalog);
        serialize(catalog);
        csvHeaders();
        csvBody();
        csvFooter();

        csvWriter.close();
    }


    @Override
    public void serializePlaces(Integer number, String name, String gazetteerId, String lat, String lon, String rel, DataExportRow collector) {
        collector.put("lat", lat);
        collector.put("lon", lon);
    }
}
