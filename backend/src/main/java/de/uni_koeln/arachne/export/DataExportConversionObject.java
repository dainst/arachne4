package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.response.search.SearchResult;

/**
 * a container class for both types of conversion objects (searchResult and catalog). if there would be more types,
 * we would prefer inheriting classes, but they are only two.
 */
public class DataExportConversionObject {
    private SearchResult searchResult;
    private Catalog catalog;

    public DataExportConversionObject(SearchResult searchResult){
        this.searchResult = searchResult;
    }

    public DataExportConversionObject(Catalog catalog){
        this.catalog = catalog;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void set(SearchResult searchResult) {
        this.catalog = null;
        this.searchResult = searchResult;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void set(Catalog catalog) {
        this.searchResult = null;
        this.catalog = catalog;
    }

    public String getType() {
        if (searchResult == null && catalog != null) {
            return "catalog";
        }
        if (searchResult != null && catalog == null) {
            return "searchResult";
        }
        return null;
    }

}
