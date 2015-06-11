package de.uni_koeln.arachne.testconfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.context.AbstractLink;
import de.uni_koeln.arachne.context.ArachneLink;
import de.uni_koeln.arachne.context.Context;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Class to hold methods that provide test data so that all test data is on one place and not spread all over the test 
 * classes.
 * 
 * @author Reimar Grabowski
 */
public class TestData {

	private final String jsonString = "{"
			+ "\"entityId\":0,"
			+ "\"type\":\"type_test\","
			+ "\"internalId\":0,"
			+ "\"datasetGroup\":\"Arachne\","
			+ "\"title\":\"Title of the Test\","
			+ "\"subtitle\":\"Subtitle of the Test\","
			+ "\"sections\":[{\"label\":\"Testdata\","
			+ "\"content\":[{\"label\":\"Testdata prefix/postfix\","
			+ "\"content\":[{\"value\":\"PrefixTest=success<hr>PostfixTest=success\"}]},"
			+ "{\"label\":\"Testdata separator\",\"content\":[{\"value\":\"first-second\"}]},"
			+ "{\"label\":\"Testdata linkField\","
			+ "\"content\":[{\"value\":\"Start<hr><a href=\\\"http://testserver.com/link1.html\\\" "
			+ "target=\\\"_blank\\\">TestLink1</a><hr><a href=\\\"http://testserver.com/link2.html\\\" "
			+ "target=\\\"_blank\\\">TestLink2</a><hr>End\"}]}]}],"
			+ "\"places\":[],"
			+ "\"dates\":[],"
			+ "\"catalogIds\":[1,2,3,4,5],\"catalogPaths\":[\"1\",\"1/2\",\"1/2/3\",\"1/2/3/4\",\"1/2/3/4/5\"],"
			+ "\"fields\":12,\"boost\":1.587285436745143,\"connectedEntities\":[1,2,3,4,5],"
			+ "\"degree\":5.0,"
			+ "\"facet_image\":[\"nein\"],"
			+ "\"facet_kategorie\":[\"test\"],"
			+ "\"facet_test\":[\"test facet value\"],"
			+ "\"facet_multivaluetest\":[\"value 1\",\"value 2\",\"value 3\"]}";
		
	private final Dataset testDataset = new Dataset();
	
	private final EntityId deletedEntity = new EntityId("test", 2L, 2L, true, 0L);
	
	public TestData() {
		// dataset
		testDataset.setArachneId(new EntityId("test", 0L, 0L, false, 0L));
				
		testDataset.setFields("test.Title", "Title of the Test");
		
		testDataset.setFields("test.Subtitle", "Subtitle of the Test");
		
		testDataset.setFields("test.DataPrefix", "success");
		testDataset.setFields("test.DataPostfix", "PostfixTest");
		
		testDataset.setFields("test.DataSeparatorBefore", "first");
		testDataset.setFields("test.DataSeparatorAfter", "second");
		
		testDataset.setFields("test.DataSearchReplace", "incorrectly replaced");
		testDataset.setFields("test.DataTrimEnd", "correctly trimmed trimmed");
		
		testDataset.setFields("test.DataLink1", "http://testserver.com/link1.html");
		testDataset.setFields("test.DataLink2", "link2");
		testDataset.setFields("test.DataNoLink1", "Start");
		testDataset.setFields("test.DataNoLink2", "End");
		
		testDataset.setFields("test.facetTest", "test facet value");
		testDataset.setFields("test.facetMultiValueTest", "value 1;value 2;value 3");
		
		final Dataset linkDataset = new Dataset();
		
		linkDataset.setArachneId(new EntityId("testContext", 0L, 1L, false, 0L));
				
		linkDataset.setFields("testContext.value1", "Test Context Value1");
		linkDataset.setFields("testContext.value3", "Test Context Value3");
		linkDataset.setFields("testContext.value4", "Test Context Value4");
		linkDataset.setFields("testContext.value5", "Test Context Value5");
		linkDataset.setFields("testContext.value6", "Test Context Value6");
		
		final ArachneLink link = new ArachneLink();
		link.setEntity1(testDataset);
		link.setEntity2(linkDataset);
		
		final List<AbstractLink> contexts = new ArrayList<AbstractLink>();
		contexts.add(link);
		
		final Context context = new Context("testContext", testDataset, contexts);
		testDataset.addContext(context);		
	}
	
	public Dataset getTestDataset() {
		return testDataset;
	}

	public EntityId getTestId() {
		return testDataset.getArachneId();
	}
	
	public EntityId getDeletedEntity() {
		return deletedEntity;
	}
	
	public String getTestJsonAsString() {
		return jsonString;
	}
	
	public byte[] getTestJson() throws JsonProcessingException, IOException {
		final ObjectMapper objectMapper = new ObjectMapper();
		JsonNode testJson = objectMapper.readTree(jsonString);
		return objectMapper.writeValueAsBytes(testJson);
	}
}
