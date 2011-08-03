package de.uni_koeln.arachne.responseobjects;

/**
 * This class implements a basic JSON response object for wrapping the database query results.
 */
public class JsonResponse {
	/**
	 * JSON response object. A POJO for wrapping the database query results.
	 */
	Long itemId;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

}
