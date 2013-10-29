package de.uni_koeln.arachne.dao;

import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.mapping.DatasetMapper;

public class DrupalSQLDao extends SQLDao {

	public Map<String,String> getNode(final int nid) {
		final List<Map<String, String>> result = jdbcTemplate.query("SELECT nid, vid, language, type FROM node WHERE nid = ? LIMIT 1",
				new Object[]{nid}, new DatasetMapper());
		if (result == null || result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}
	
	public Map<String,String> getRevision(final int vid) {
		final List<Map<String, String>> result = jdbcTemplate
				.query("SELECT body, title, format FROM node_revisions WHERE vid = ? LIMIT 1",
						new Object[]{vid}, new DatasetMapper());
		if (result == null || result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}
	
	public String getTeaser(final int vid) {
		return jdbcTemplate.queryForObject("SELECT field_teaser_value FROM content_type_project WHERE vid = ?",
				new Object[]{vid}, String.class);
	}
	
	public List<Map<String,String>> getLinks(final int vid) {
		return jdbcTemplate.query("SELECT field_links_url, field_links_title FROM content_field_links WHERE vid = ? AND field_links_url IS NOT NULL",
				new Object[]{vid}, new DatasetMapper());
	}
	
	public List<Map<String,String>> getImages(final int vid) {
		return jdbcTemplate.query("SELECT filepath FROM content_field_images JOIN files ON field_images_fid = fid WHERE vid = ?",
			new Object[]{vid}, new DatasetMapper());
	}

	public List<Map<String,String>> getMenuEntries(final String name) {
		return jdbcTemplate.query("SELECT mlid, plid, link_path, link_title, has_children FROM menu_links WHERE menu_name LIKE ? ORDER BY weight DESC",
				new Object[]{name}, new DatasetMapper());	
	}

	public List<Map<String,String>> getTeasers(final String language) {
		return jdbcTemplate.query("SELECT nid FROM node WHERE language LIKE ? AND type LIKE 'project' AND promote = 1",
				new Object[]{language}, new DatasetMapper());	
	}

	public List<Map<String, String>> getNews() {
		return jdbcTemplate.query("SELECT nid FROM node WHERE type LIKE 'news' ORDER BY created DESC LIMIT 10", new DatasetMapper());	
	}

}
