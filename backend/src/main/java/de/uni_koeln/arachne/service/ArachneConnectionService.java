package de.uni_koeln.arachne.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ArachneConnectionDao;

@Service
public class ArachneConnectionService {
	
	@Autowired
	private ArachneConnectionDao arachneConnectionDao;
	
	public List<String> getConnectionMap(String type) {
		return arachneConnectionDao.getConnectionList(type);
	}
}
