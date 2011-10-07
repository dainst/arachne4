package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.GenericFieldDao;

@Service
public class GenericFieldService {
	@Autowired
	protected GenericFieldDao genericFieldDao;
	
	public List<Long> getIdByFieldId(String tableName, String field1, Long field1Id, String field2) {
		return genericFieldDao.getIdByFieldId(tableName, field1, field1Id, field2);
	}
}
