package de.uni_koeln.arachne.service;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_koeln.arachne.dao.SessionDao;
import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.Session;
import de.uni_koeln.arachne.mapping.UserAdministration;

/**
 * This class allows to query the current users rights. 
 * It looks up the session, the corresponding user and the groups in the database
 * @author Rasmus Krempel
 * @author Sebastian Cuy
 */
@Service("userRightsService")
@Scope(value="request",proxyMode=ScopedProxyMode.INTERFACES)
public class UserRightsService implements IUserRightsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRightsService.class);
	
	/**
	 * User management DAO instance.
	 */
	@Autowired
	private transient UserVerwaltungDao userVerwaltungDao; 
	
	/**
	 * Session management DAO instance.
	 */
	@Autowired
	private transient SessionDao sessionDao; 
	
	/**
	 * Flag that indicates if the User Data is loaded.
	 */
	private transient boolean isSet = false;

	/**
	 * The Arachne user data set.
	 */
	private transient UserAdministration arachneUser = null;

	/**
	 * Method initializing access to the user data. 
	 * If the user data is not fetched yet, it fetches the user name from the session, gets the database row with the user data and formats it 
	 */
	private void initializeUserData() {
		if (!isSet) {
			
			final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
	                .getRequestAttributes()).getRequest();

			LOGGER.debug("Session-ID: " + request.getSession().getId());
			final Session session = sessionDao.findById(request.getSession().getId());
			if (session == null) {
				arachneUser = userVerwaltungDao.findByName("anonymous");
			} else {
				arachneUser = session.getUserAdministration();
			}
			
			isSet = true;		
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#setUserSolr()
	 */
	@Override
	public void setUserSolr() {
		arachneUser = new UserAdministration();
		arachneUser.setUsername(SOLR_INDEXING);
		arachneUser.setAll_groups(true);
		this.isSet = true;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#isUserSolr()
	 */
	@Override
	public boolean isUserSolr() {
		return isSet && SOLR_INDEXING.equals(arachneUser.getUsername());
	}

	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#getCurrentUser()
	 */
	@Override
	public UserAdministration getCurrentUser() {
		initializeUserData();
		return arachneUser;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#reset()
	 */
	@Override
	public void reset() {
		arachneUser = null;
		isSet = false;
	}
}
