package de.uni_koeln.arachne.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringJUnitConfig(locations = { "classpath:test-context.xml" })
@WebAppConfiguration
public class ITestXmlConfigUtil {
	private transient XmlConfigUtil xmlConfigUtil;

	/* ~~(org/openrewrite/staticanalysis/LambdaBlockToExpression)~~> */@Test
	public void testForDocumentCompleteness() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/main/webapp"));

		assertNotNull(xmlConfigUtil.getDocument("bauwerksteil"));
		assertNotNull(xmlConfigUtil.getDocument("bauwerk"));
		assertNotNull(xmlConfigUtil.getDocument("befund"));
		assertNotNull(xmlConfigUtil.getDocument("buchseite"));
		assertNotNull(xmlConfigUtil.getDocument("buch"));
		assertNotNull(xmlConfigUtil.getDocument("fabric"));
		assertNotNull(xmlConfigUtil.getDocument("gruppen"));
		assertNotNull(xmlConfigUtil.getDocument("gruppierung"));
		assertNotNull(xmlConfigUtil.getDocument("inschrift"));
		assertNotNull(xmlConfigUtil.getDocument("literatur"));
		assertNotNull(xmlConfigUtil.getDocument("mainabstract"));
		assertNotNull(xmlConfigUtil.getDocument("marbilder"));
		assertNotNull(xmlConfigUtil.getDocument("modell3d"));
		assertNotNull(xmlConfigUtil.getDocument("morphology"));
		assertNotNull(xmlConfigUtil.getDocument("niton"));
		assertNotNull(xmlConfigUtil.getDocument("objekt"));
		assertNotNull(xmlConfigUtil.getDocument("ort"));
		assertNotNull(xmlConfigUtil.getDocument("person"));
		assertNotNull(xmlConfigUtil.getDocument("realien"));
		assertNotNull(xmlConfigUtil.getDocument("relief"));
		assertNotNull(xmlConfigUtil.getDocument("reproduktion"));
		assertNotNull(xmlConfigUtil.getDocument("rezeption"));
		assertNotNull(xmlConfigUtil.getDocument("sammlungen"));
		assertNotNull(xmlConfigUtil.getDocument("sarkophag"));
		assertNotNull(xmlConfigUtil.getDocument("topographie"));
		assertNotNull(xmlConfigUtil.getDocument("typus"));

		xmlConfigUtil = null;
	}
}
