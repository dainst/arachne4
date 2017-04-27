package de.uni_koeln.arachne.util.sql;

import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;

/**
 * Helper class to hold a catalog entry and extended information about the catalog the entry is part of.
 *
 * @author Sebastian Cuy
 * @author Reimar Grabowski
 */
public class CatalogEntryExtended {

	final private CatalogEntry entry;

	final private String catalogTitle;

	final private String catalogAuthor;

	final private String projectId;

	final private Boolean isPublic;
	
	/**
	 * Constructor to set the extended infos.
	 * @param entry The {@link CatalogEntry}.
	 * @param catalogTitle The title of the catalog the entry is part of.
	 * @param catalogAuthor The author of the catalog the entry is part of.
	 * @param isPublic If the catalog the entry is part of is public.
	 */
	public CatalogEntryExtended(CatalogEntry entry, String catalogTitle, String catalogAuthor, String projectId, Boolean isPublic) {
		this.entry = entry;
		this.catalogTitle = catalogTitle;
		this.catalogAuthor = catalogAuthor;
		this.projectId = projectId;
		this.isPublic = isPublic;
	}

	/**
	 * Getter for the {@link CatalogEntry}.
	 * @return The catalog entry.
	 */
	public CatalogEntry getEntry() {
		return entry;
	}

	/**
	 * Getter for the title of the catalog the entry is part of.
	 * @return The title.
	 */
	public String getCatalogTitle() {
		return catalogTitle;
	}

	/**
	 * Getter for the author of the catalog the entry is part of.
	 * @return The author.
	 */
	public String getCatalogAuthor() { return catalogAuthor; }

	/**
	 * Getter for the author of the catalog the entry is part of.
	 * @return The author.
	 */
	public String getProjectId() { return projectId; }

	/**
	 * Whether the catalog the entry belongs to is publicly accessible.
	 * @return <code>true</code> if the catalog is public, otherwise <code>false</code>
	 */
	public Boolean isPublic() { return isPublic; }

}
