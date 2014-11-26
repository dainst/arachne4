package de.uni_koeln.arachne.response.link;

/**
 * Represents an external link to a browser or viewer that can be rendered in the frontend.
 * @author scuy
 */
public class ExternalLink {

	private String label;
	
	private String url;
	
	public ExternalLink(String label, String url) {
		this.label = label;
		this.url = url;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
