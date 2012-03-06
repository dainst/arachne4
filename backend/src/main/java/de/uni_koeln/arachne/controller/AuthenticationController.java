package de.uni_koeln.arachne.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.SessionDao;
import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.Session;
import de.uni_koeln.arachne.mapping.UserAdministration;

/**
 * Handles http requests (currently only get) for <code>/auth<code>.
 */
@Controller
public class AuthenticationController {
	
	@Autowired
	private UserVerwaltungDao userDao;
	
	@Autowired
	private SessionDao sessionDao;
	
	/**
	 * Handles login
	 * @return Session a session object, also containing the user and his groups
	 */
	@RequestMapping(value="/sessions", method=RequestMethod.POST)
	public @ResponseBody Session createSession(
			@RequestParam("user") String username, 
			@RequestParam("password") String encryptedPassword,
			HttpServletResponse response,
			HttpServletRequest request) {
		
		UserAdministration user = userDao.findByName(username);
		if (user != null && user.getPassword().equals(encryptedPassword)) {
			sessionDao.deleteAllSessionsForUser(user.getId());
			Session session = new Session();
			session.setUserAdministration(user);
			session.setTimestamp(new Date());
			session.setSid(request.getSession().getId());
			session.setIpaddress(request.getRemoteAddr());
			session.setUseragent(request.getHeader("User-Agent"));
			response.setStatus(201);
			response.setHeader("Location", request.getRequestURL() + "/" + session.getSid());
			return sessionDao.saveSession(session);
		} else {
			response.setStatus(403);
			return null;
		}
		
	}
	
	/**
	 * Handles logout
	 * @return
	 */
	@RequestMapping(value="/sessions/{id}", method=RequestMethod.DELETE)
	public void destroySession(
			HttpServletResponse response,
			HttpServletRequest request,
			@PathVariable("id") String id) {
		
		sessionDao.deleteSession(request.getSession().getId());
		request.getSession().invalidate();
		response.setStatus(204);
		
	}
	
	
}
