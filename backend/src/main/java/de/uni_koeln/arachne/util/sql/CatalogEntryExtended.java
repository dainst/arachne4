package de.uni_koeln.arachne.util.sql;

import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;

/**
 * Helper class to hold catalog entries and extended infos.
 *
 * @author Sebastian Cuy
 *
 */
public class CatalogEntryExtended {

	final private CatalogEntry entry;

	final private String catalogTitle;

	final private String catalogAuthor;

	final private Boolean isPublic;
	
	public CatalogEntryExtended(CatalogEntry entry, String catalogTitle, String catalogAuthor, Boolean isPublic) {
		this.entry = entry;
		this.catalogTitle = catalogTitle;
		this.catalogAuthor = catalogAuthor;
		this.isPublic = isPublic;
	}

	public CatalogEntry getEntry() {
		return entry;
	}

	public String getCatalogTitle() {
		return catalogTitle;
	}

	public String getCatalogAuthor() { return catalogAuthor; }

	public Boolean isPublic() { return isPublic; }

}
