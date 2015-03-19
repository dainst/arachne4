package de.uni_koeln.arachne.dao;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations = {"classpath:test-context.xml"}) 
public class ITestArachneEntityDao {
	
	@Autowired
	private transient ArachneEntityDao arachneEntityDao;

	@Test
	public void testGetEntityById() {
		assertEquals(1L, (long)arachneEntityDao.getByEntityID(1).getEntityId());
	}
	
	@Test
	public void testGetByTablenameAndInternalKey() {
		assertEquals(1L, (long)arachneEntityDao.getByTablenameAndInternalKey("objekt", 1).getForeignKey());
	}
	
	@Test
	public void testGetByLimitedEntityIdRange() {
		assertEquals(10, arachneEntityDao.getByLimitedEntityIdRange(0, 10).size());
	}

}
