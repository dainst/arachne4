package de.uni_koeln.arachne.util.sql;

/**
 * Helper class to hold catalog entry information. 
 * 
 * @author Reimar Grabowski
 *
 */
public class CatalogEntryInfo {

	final private Long catalogId;
	
	final private String path;
	
	public CatalogEntryInfo(long catalogId, String path, long catalogEntryId) {
		this.catalogId = catalogId;
		this.path = path + "/" + catalogEntryId;
	}

	public Long getCatalogId() {
		return catalogId;
	}

	public String getPath() {
		return path;
	}
}
