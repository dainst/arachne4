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
	
	private static Logger logger = LoggerFactory.getLogger(CMSService.class);
	
	@Value("#{config.drupalUrl}")
	private String drupalUrl;
	
	@Autowired
	private transient DrupalSQLDao dao;

	public Node getNodeById(Integer id) {
		
		Node page = new Node();
		
		Map<String, String> node = dao.getNode(id);
		if (node == null) return null;
		
		page.setId(Integer.parseInt(node.get("node.nid")));
		page.setLanguage(node.get("node.language"));
		int vid = Integer.parseInt(node.get("node.vid"));
		
		Map<String, String> rev = dao.getRevision(vid);
		page.setTitle(rev.get("node_revisions.title"));
		page.setBody(rev.get("node_revisions.body"));
		page.setFormat(rev.get("node_revisions.format"));
		
		// process body html for better output
		String body = page.getBody();
		body = body.replaceAll("([^>])(\r\n)", "$1<br/>");
		if("2".equals(page.getFormat())) { // content is "full html"
			String replacement = getAbsoluteImageUrl("sites/default/files/$2");
			body = body.replaceAll("(sites/default/files/)([^\"])", replacement);
		}
		page.setBody(body);
		
		// only project nodes contain teasers, links and images
		if ("project".equals(node.get("node.type"))) {
			
			String teaser = dao.getTeaser(vid);
			if (teaser != null && !teaser.isEmpty()) 
				page.setTeaser(teaser);
		
			List<Map<String, String>> links = dao.getLinks(vid);
			if (links != null && !links.isEmpty()) {
				ArrayList<Link> linkList = new ArrayList<Link>();
				for (Map<String, String> link : links) {
					Link newLink = new Link();
					logger.info("link: {}", link);
					for (String key : link.keySet()) 
						logger.info("key: {}, value: {}", key, link.get(key));
					newLink.setHref(link.get("content_field_links.field_links_url"));
					newLink.setTitle(link.get("content_field_links.field_links_title"));
					linkList.add(newLink);
				}
				page.setLinks(linkList);
			}
			
			List<Map<String, String>> images = dao.getImages(vid);
			if (images != null && !images.isEmpty()) {
				ArrayList<String> imageList = new ArrayList<String>();
				for (Map<String, String> image : images) {
					imageList.add(getAbsoluteImageUrl(image.get("files.filepath")));
				}
				page.setImages(imageList);
			}
			
		}
		
		return page;
		
	}

	public Map<String, MenuEntry> getMenuByName(String name) {
		List<Map<String, String>> menuEntries = dao.getMenuEntries(name);
		Map<String,MenuEntry> map = new HashMap<String, MenuEntry>();
		for (Map<String, String> menuEntry : menuEntries) {
			MenuEntry newMenuEntry = new MenuEntry();
			newMenuEntry.setId(menuEntry.get("menu_links.mlid"));
			newMenuEntry.setParent(menuEntry.get("menu_links.plid"));
			newMenuEntry.setPath(menuEntry.get("menu_links.link_path"));
			newMenuEntry.setTitle(menuEntry.get("menu_links.link_title"));
			map.put(newMenuEntry.getId(), newMenuEntry);
		}
		// create children array
		for (String id : map.keySet()) {
			if (map.get(id).getParent() == null) continue;
			MenuEntry parent = map.get(map.get(id).getParent());
			if (parent == null) continue;
			List<String> children = parent.getChildren();
			if (children == null) {
				children = new ArrayList<String>();
				parent.setChildren(children);
			}
			children.add(id);
		}
		return map;
	}

	public List<Node> getTeasers(String language) {
		List<Map<String,String>> teasers = dao.getTeasers(language);
		ArrayList<Node> result = new ArrayList<Node>();
		for (Map<String,String> teaser : teasers) {
			result.add(getNodeById(Integer.parseInt(teaser.get("node.nid"))));
		}
		return result;
	}
	
	private String getAbsoluteImageUrl(String relImageUrl) {
		String cacheUrl = relImageUrl
				.replaceFirst("sites/default/files/", "sites/default/files/imagecache/project_node/");
		return drupalUrl + "/" + cacheUrl;
	}

}
