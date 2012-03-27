package de.uni_koeln.arachne.dao;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.UserAdministration;


@Repository("UserVerwaltungDao")
public class UserVerwaltungDao extends AbstractHibernateTemplateDao{

	public UserAdministration findById(long id) {
		return (UserAdministration) hibernateTemplate.get(UserAdministration.class, id);
	}

	public UserAdministration findByName(String user) {
		String hql = "from UserAdministration as user WHERE user.username LIKE ?";
		UserAdministration result = null;
		if (hibernateTemplate.find(hql,user).size() > 0) {
			result = (UserAdministration)hibernateTemplate.find(hql,user).get(0);
		}
		return result;
	}
}
