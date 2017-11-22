package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class Catalog2HtmlConverter extends BaseHtmlConverter<Catalog> {

    @Override
    protected boolean supports(Class<?> aClass) {
        return aClass == Catalog.class;
    }

    @Override
    protected void writeInternal(Catalog catalog, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        setExportMetaData(catalog.getAuthor() + ": " + catalog.getRoot().getLabel());
        writer = new OutputStreamWriter(httpOutputMessage.getBody());
        htmlHeader();
        htmlCatalogFrontMatter(catalog);
        htmlCatalog(catalog);
        htmlFooter();
        writer.close();
    }






}
