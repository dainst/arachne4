package de.uni_koeln.arachne.mapping;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;

@TestMethodOrder(MethodName.class)
public class TestCatalog {

	public static Catalog catalog;

	@BeforeAll
	public static void setUp() throws JsonParseException, JsonMappingException,
			IOException {
		URL resource = TestCatalog.class
				.getResource("/WEB-INF/json/catalog.json");
		ObjectMapper mapper = new ObjectMapper();
		catalog = mapper.readValue(
				Resources.toString(resource, Charsets.UTF_8), Catalog.class);
	}

	/* ~~(org/openrewrite/staticanalysis/LambdaBlockToExpression)~~> */@Test
	public void test1Deserialization() {
		assertNotNull(catalog);
		assertNotNull(catalog.getRoot());
		assertNotNull(catalog.getRoot().getChildren());
	}
}
