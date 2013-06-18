package de.uni_koeln.arachne.response;

import java.util.List;

public class Node {
	
	private int id;
	private String title;
	private String teaser;
	private String body;
	private List<String> images;
	private List<Link> links;
	private String language;
	private String format;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTeaser() {
		return teaser;
	}
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public List<String> getImages() {
		return images;
	}
	public void setImages(List<String> images) {
		this.images = images;
	}
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}

	public static class Link {
		
		private String href;
		private String title;
		public String getHref() {
			return href;
		}
		public void setHref(String href) {
			this.href = href;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		
	}

}
