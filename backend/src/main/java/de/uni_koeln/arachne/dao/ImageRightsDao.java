package de.uni_koeln.arachne.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.ImageRightsGroup;

@Repository
public class ImageRightsDao {

	@Autowired
    private transient SessionFactory sessionFactory;
	
	public ImageRightsGroup findByName(final String name) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from ImageRightsGroup where name like :name")
				.setString("name", name);
		
		@SuppressWarnings("unchecked")
		final List<ImageRightsGroup> result = (List<ImageRightsGroup>) query.list();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

}
