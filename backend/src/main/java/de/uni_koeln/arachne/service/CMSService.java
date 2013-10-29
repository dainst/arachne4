package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.DrupalSQLDao;
import de.uni_koeln.arachne.response.MenuEntry;
import de.uni_koeln.arachne.response.Node;
import de.uni_koeln.arachne.response.Node.Link;

@Service
public class CMSService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CMSService.class);
	
	@Value("#{config.drupalUrl}")
	private transient String drupalUrl;
	
	@Autowired
	private transient DrupalSQLDao dao;

	public Node getNodeById(Integer id) { // NOPMD
		
		final Node page = new Node();
		
		final Map<String, String> node = dao.getNode(id);
		if (node == null) {
			return null;
		}
		
		page.setId(Integer.parseInt(node.get("node.nid")));
		page.setLanguage(node.get("node.language"));
		final int vid = Integer.parseInt(node.get("node.vid"));
		
		final Map<String, String> rev = dao.getRevision(vid);
		page.setTitle(rev.get("node_revisions.title"));
		page.setBody(rev.get("node_revisions.body"));
		page.setFormat(rev.get("node_revisions.format"));
		
		// process body html for better output
		String body = page.getBody();
		body = body.replaceAll("([^>])(\r\n)", "$1<br/>");
		if("2".equals(page.getFormat())) { // content is "full html"
			final String replacement = getAbsoluteImageUrl("sites/default/files/$2");
			body = body.replaceAll("(sites/default/files/)([^\"])", replacement);
		}
		page.setBody(body);
		
		// only project nodes contain teasers, links and images
		if ("project".equals(node.get("node.type"))) {
			
			final String teaser = dao.getTeaser(vid);
			if (teaser != null && !teaser.isEmpty()) {
				page.setTeaser(teaser);
			}
		
			final List<Map<String, String>> links = dao.getLinks(vid);
			if (links != null && !links.isEmpty()) {
				final ArrayList<Link> linkList = new ArrayList<Link>();
				for (final Map<String, String> link : links) {
					final Link newLink = new Link();
					LOGGER.debug("link: {}", link);
					for (final String key : link.keySet()) {
						LOGGER.debug("key: {}, value: {}", key, link.get(key));
					}
					newLink.setHref(link.get("content_field_links.field_links_url"));
					newLink.setTitle(link.get("content_field_links.field_links_title"));
					linkList.add(newLink);
				}
				page.setLinks(linkList);
			}
			
			final List<Map<String, String>> images = dao.getImages(vid);
			if (images != null && !images.isEmpty()) {
				final ArrayList<String> imageList = new ArrayList<String>();
				for (final Map<String, String> image : images) {
					imageList.add(getAbsoluteImageUrl(image.get("files.filepath")));
				}
				page.setImages(imageList);
			}
			
		}
		
		return page;
		
	}

	public Map<String, MenuEntry> getMenuByName(final String name) {
		final List<Map<String, String>> menuEntries = dao.getMenuEntries(name);
		final Map<String,MenuEntry> map = new HashMap<String, MenuEntry>();
		for (final Map<String, String> menuEntry : menuEntries) {
			final MenuEntry newMenuEntry = new MenuEntry();
			newMenuEntry.setId(menuEntry.get("menu_links.mlid"));
			newMenuEntry.setParent(menuEntry.get("menu_links.plid"));
			newMenuEntry.setPath(menuEntry.get("menu_links.link_path"));
			newMenuEntry.setTitle(menuEntry.get("menu_links.link_title"));
			map.put(newMenuEntry.getId(), newMenuEntry);
		}
		// create children array
		for (String id : map.keySet()) { // NOPMD
			if (map.get(id).getParent() == null) {
				continue;
			}
			final MenuEntry parent = map.get(map.get(id).getParent());
			if (parent == null) {
				continue;
			}
			List<String> children = parent.getChildren();
			if (children == null) {
				children = new ArrayList<String>();
				parent.setChildren(children);
			}
			children.add(id);
		}
		return map;
	}

	public List<Node> getTeasers(final String language) {
		final List<Map<String,String>> teasers = dao.getTeasers(language);
		final ArrayList<Node> result = new ArrayList<Node>();
		for (final Map<String,String> teaser : teasers) {
			result.add(getNodeById(Integer.parseInt(teaser.get("node.nid"))));
		}
		return result;
	}
	
	public List<Node> getNews() {
		final List<Map<String,String>> news = dao.getNews();
		final ArrayList<Node> result = new ArrayList<Node>();
		for (final Map<String,String> newsItem : news) {
			result.add(getNodeById(Integer.parseInt(newsItem.get("node.nid"))));
		}
		return result;
	}
	
	private String getAbsoluteImageUrl(final String relImageUrl) {
		final String cacheUrl = relImageUrl
				.replaceFirst("sites/default/files/", "sites/default/files/imagecache/project_node/");
		return drupalUrl + "/" + cacheUrl;
	}

}
