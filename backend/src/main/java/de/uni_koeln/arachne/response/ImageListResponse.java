package de.uni_koeln.arachne.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response class for image list requests.
 * @author Reimar Grabowski
 */
@JsonInclude(Include.NON_EMPTY)
public class ImageListResponse {
	private final List<Image> images;
	
	/**
	 * Constructor initialzing 'imageList'.
	 * @param imageList The image list.
	 */
	public ImageListResponse(final List<Image> imageList) {
		images = imageList;
	}
	
	public List<Image> getImages() {
		return images;
	}
}
