package de.uni_koeln.arachne.util.sql;

import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;

/**
 * Helper class to hold catalog entry information.
 *
 * @author Reimar Grabowski
 *
 */
public class CatalogEntryInfo {

    final private Long catalogId;

    final private String path;

    /**
     * Constructor to set all fields.
     * @param catalogId The catalog id of the catalog the entry is part of.
     * @param path The path of this {@link CatalogEntry}.
     * @param catalogEntryId The id of the {@link CatalogEntry}
     */
    public CatalogEntryInfo(long catalogId, String path, long catalogEntryId) {
        this.catalogId = catalogId;
        this.path = path + "/" + catalogEntryId;
    }

    /**
     * Getter for the catalog id of the catalog the entry is part of.
     * @return The catalog id.
     */
    public Long getCatalogId() {
        return catalogId;
    }

    /**
     * Retrieves the path of the catalog entry. The path is a 'slash-separated' string starting with the catalog id 
     * followed by the catalog entry ids that 'lead' to an entry.
     * @return The path.
     */
    public String getPath() {
        return path;
    }
}