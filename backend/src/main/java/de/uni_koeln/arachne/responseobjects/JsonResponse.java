package de.uni_koeln.arachne.responseobjects;

public class JsonResponse {
	/**
	 * JSON response object. A POJO for wrapping the database query results.
	 * @author Reimar Grabowski
	 */
	Long itemId;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

}
