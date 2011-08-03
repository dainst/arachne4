package de.uni_koeln.arachne.dao;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.UserAdministration;


@Repository("UserVerwaltungDao")
public class UserVerwaltungDao extends HibernateTemplateDao{

	public UserAdministration findById(long id) {
		return (UserAdministration)hibernateTemplate.get(UserAdministration.class, id);
	}
}
