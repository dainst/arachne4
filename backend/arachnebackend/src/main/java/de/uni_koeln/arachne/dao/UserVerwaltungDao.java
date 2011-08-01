package de.uni_koeln.arachne.dao;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.UserVerwaltung;


@Repository("UserVerwaltungDao")
public class UserVerwaltungDao extends AbstractDao{

	public UserVerwaltung findById(long id) {
		return (UserVerwaltung)hibernateTemplate.get(UserVerwaltung.class, id);
	}
}
