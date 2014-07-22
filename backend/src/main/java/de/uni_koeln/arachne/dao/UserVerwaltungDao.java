package de.uni_koeln.arachne.dao;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.util.RegisterFormValidationUtil;


@Repository("UserVerwaltungDao")
public class UserVerwaltungDao {

	@Autowired
	private transient HibernateTemplate hibernateTemplate;
	
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
	 * Method to find an user by the email-authentification token
	 * @param token
	 * @return
	 */
	public UserAdministration findByAuthToken(final String token) {
		final String hql = "from UserAdministration as user WHERE user.emailAuth LIKE ?";
		UserAdministration result = null;
		if(hibernateTemplate.find(hql, token).size() > 0) {
			result = (UserAdministration)hibernateTemplate.find(hql, token).get(0);
		}
		return result;
	}
	
	public UserAdministration updateUser(final UserAdministration user) {
		if(user != null) {
			hibernateTemplate.update(user);
		}
		return user;
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
