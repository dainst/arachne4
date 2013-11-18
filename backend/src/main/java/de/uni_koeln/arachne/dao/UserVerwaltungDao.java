package de.uni_koeln.arachne.dao;

import java.io.Serializable;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.util.RegisterFormValidationUtil;


@Repository("UserVerwaltungDao")
public class UserVerwaltungDao extends AbstractHibernateTemplateDao{

	public UserAdministration findById(final long uid) {
		return (UserAdministration) hibernateTemplate.get(UserAdministration.class, uid);
	}

	public UserAdministration findByName(final String user) {
		final String hql = "from UserAdministration as user WHERE user.username LIKE ?";
		UserAdministration result = null;
		if (hibernateTemplate.find(hql,user).size() > 0) {
			result = (UserAdministration)hibernateTemplate.find(hql,user).get(0);
		}
		return result;
	}
	
	/**
	 * Method to create a new User-Account in the database
	 * @param user
	 * @return
	 */
	public boolean newUser(RegisterFormValidationUtil user) {
		Serializable serializable = hibernateTemplate.save(user);
		
		if(serializable != null) {
			return true;
		}
		
		return false;
	}
}
