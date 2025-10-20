package de.uni_koeln.arachne.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;

@SpringJUnitConfig(locations = { "classpath:test-context.xml" })
@WebAppConfiguration
public class ITestArachneEntityDao {

	@Autowired
	private transient ArachneEntityDao arachneEntityDao;

	/* ~~(org/openrewrite/staticanalysis/LambdaBlockToExpression)~~> */@Test
	public void testGetEntityById() {
		assertEquals(1L, (long) arachneEntityDao.getByEntityID(1).getEntityId());
	}

	@Test
	public void testGetByTablenameAndInternalKey() {
		assertEquals(1L, (long) arachneEntityDao.getByTablenameAndInternalKey("objekt", 1).getForeignKey());
	}

	@Test
	public void testGetByLimitedEntityIdRange() {
		assertEquals(10, arachneEntityDao.getByLimitedEntityIdRange(0, 10).size());
	}

}
