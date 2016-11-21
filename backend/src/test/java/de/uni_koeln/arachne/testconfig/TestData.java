package de.uni_koeln.arachne.testconfig;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import de.uni_koeln.arachne.context.AbstractLink;
import de.uni_koeln.arachne.context.ArachneLink;
import de.uni_koeln.arachne.context.Context;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Place;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.response.search.SearchResultFacetValue;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Class to hold methods that provide test data so that all test data is on one place and not spread all over the test 
 * classes.
 * 
 * @author Reimar Grabowski
 */
public class TestData {

	public static final String jsonString = "{"
			+ "\"entityId\":1,"
			+ "\"type\":\"type_test\","
			+ "\"internalId\":123,"
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
			+ "\"images\":[{\"imageId\":12345,\"imageSubtitle\":\"Image 12345\"}],"
			+ "\"imageSize\":2,"
			+ "\"fields\":12,\"boost\":1.587285436745143,\"connectedEntities\":[1,2,3,4,5],"
			+ "\"degree\":5.0,"
			+ "\"facet_image\":[\"nein\"],"
			+ "\"facet_kategorie\":[\"test\"],"
			+ "\"facet_test\":[\"test facet value\"],"
			+ "\"facet_multivaluetest\":[\"value 1\",\"value 2\",\"value 3\"],"
			+ "\"facet_includetest\":[\"include value 1\",\"include value 2\"]}";
	
	public static final String zoomifyImageProperties 
			= "<IMAGE_PROPERTIES WIDTH=\"1600\" HEIGHT=\"1000\" NUMTILES=\"28\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />";
	
	public static final EntityId deletedEntity = new EntityId("test", 432L, 32L, true, 0L);

	public static Dataset getTestDataset() {
		final Dataset testDataset = new Dataset();
		
		testDataset.setArachneId(new EntityId("test", 123L, 1L, false, 1L));
				
		testDataset.setFields("test.otherId", "1234567890");
		testDataset.setFields("test.anotherId", "a1b2c3d4");
		
		testDataset.setFields("test.filename", "test_filename.ext");
				
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
		
		testDataset.setFields("test.DataEditorSection", "for editors only");
		
		testDataset.setFields("test.facetTest", "test facet value");
		testDataset.setFields("test.facetMultiValueTest", "value 1;value 2;value 3");
		testDataset.setFields("test.includeTestFacetValue1", "include value 1");
		testDataset.setFields("test.includeTestFacetValue2", "include value 2");
		
		final List<de.uni_koeln.arachne.response.Image> imageList = new ArrayList<>();
		de.uni_koeln.arachne.response.Image image = new de.uni_koeln.arachne.response.Image();
		image.setImageId(123L);
		image.setImageSubtitle("Image 123");
		imageList.add(image);
		image = new de.uni_koeln.arachne.response.Image();
		image.setImageId(321L);
		image.setImageSubtitle("Image 321");
		imageList.add(image);
		image = new de.uni_koeln.arachne.response.Image();
		image.setImageId(12345L);
		image.setImageSubtitle("Image 12345");
		imageList.add(image);
		image = new de.uni_koeln.arachne.response.Image();
		image.setImageId(54321L);
		image.setImageSubtitle("Image 54321");
		imageList.add(image);
		testDataset.setImages(imageList);
		testDataset.setThumbnailId(123L);
		
		final Dataset linkDataset = new Dataset();
		
		linkDataset.setArachneId(new EntityId("testContext", 12L, 2L, false, null));
				
		linkDataset.setFields("testContext.value1", "Test Context Value1");
		linkDataset.setFields("testContext.value3", "Test Context Value3");
		linkDataset.setFields("testContext.value4", "Test Context Value4");
		linkDataset.setFields("testContext.value5", "Test Context Value5");
		linkDataset.setFields("testContext.value6", "Test Context Value6");
		linkDataset.setFields("testContext.value7", "Test Context Value7");
		
		final ArachneLink link = new ArachneLink();
		link.setEntity1(testDataset);
		link.setEntity2(linkDataset);
		
		final List<AbstractLink> contexts = new ArrayList<AbstractLink>();
		contexts.add(link);
		
		final Context context = new Context("testContext", testDataset, contexts);
		testDataset.addContext(context);
		return testDataset;
	}
	
	public static byte[] getTestJson() throws JsonProcessingException, IOException {
		final ObjectMapper objectMapper = new ObjectMapper();
		JsonNode testJson = objectMapper.readTree(jsonString);
		return objectMapper.writeValueAsBytes(testJson);
	}
	
	public static BufferedImage getTestImageJPEG() throws IOException {
		final URL resource = TestData.class.getResource("/WEB-INF/images/greif.jpeg");
		final InputStream stream = Resources.asByteSource(resource).openStream();
		final BufferedImage result = ImageIO.read(stream);
		stream.close();
		return result;
	}
	
	public static byte[] getScaledTestImageJPEG(int width, int height) throws IOException {
		final BufferedImage origImage = getTestImageJPEG();
		if (width <= 0) {
			width = origImage.getWidth();
		}
		if (height <= 0) {
			height = origImage.getHeight();
		}
		final Image image = origImage.getScaledInstance(width, height, Image.SCALE_FAST);
		final BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		resultImage.getGraphics().drawImage(image, 0, 0, null);
		
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		ImageIO.write(resultImage, "jpeg", byteOutStream);
		byteOutStream.flush();
		final byte[] result = byteOutStream.toByteArray();
		byteOutStream.close();
		
		return result;
	}
	
	public static SearchResult getDefaultSearchResult() {
		final SearchResult result = new SearchResult();
		result.addSearchHit(new SearchHit(1l, "test", "testServer.com/entity/1" ,"Test title", "Test subtitle", 1l
				, new ArrayList<Place>(), null));
		result.addSearchHit(new SearchHit(2l, "test", "testServer.com/entity/2" ,"Test title 1", "Test subtitle 1", 2l
				, new ArrayList<Place>(), null));
		result.setSize(2);
		final SearchResultFacet facet1 = new SearchResultFacet("facet_test1");
		facet1.addValue(new SearchResultFacetValue("test1_value1b", "", 1));
		facet1.addValue(new SearchResultFacetValue("test1_value1a", "", 2));
		final SearchResultFacet facet2 = new SearchResultFacet("facet_test2");
		facet2.addValue(new SearchResultFacetValue("test2_value1", "", 4));
		final SearchResultFacet facet3 = new SearchResultFacet("facet_test3");
		facet3.addValue(new SearchResultFacetValue("test3_value1", "", 13));
		facet3.addValue(new SearchResultFacetValue("test3_value2", "", 12));
		facet3.addValue(new SearchResultFacetValue("test3_value3", "", 11));
		final List<SearchResultFacet> facets = new ArrayList<SearchResultFacet>();
		facets.add(facet1);
		facets.add(facet2);
		facets.add(facet3);
		result.setFacets(facets);
		return result;
	}
}
