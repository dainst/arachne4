package de.uni_koeln.arachne.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.ImageRightsGroup;

@Repository
public class ImageRightsDao {

	@Autowired
	private transient HibernateTemplate hibernateTemplate;
	
	public ImageRightsGroup findByName(final String name) {
		@SuppressWarnings("unchecked")
		final List<ImageRightsGroup> result = (List<ImageRightsGroup>) hibernateTemplate.find("from ImageRightsGroup where name like ?", name);
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

}
