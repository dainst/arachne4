package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

@RunWith(MockitoJUnitRunner.class)
public class TestImageService {

	@Mock
	private DataIntegrityLogService dataIntegrityLogService;
	
	@Mock
	private GenericSQLDao genericSQLDao;
	
	@SuppressWarnings("serial")
	@InjectMocks
	private final ImageService imageService = new ImageService(new ArrayList<String>() {{ add("noImage"); }});
	
	private final Dataset testDataset = TestData.getTestDataset();
	
	private final EntityId entityId = testDataset.getArachneId();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		when(genericSQLDao.getImageList(entityId.getTableName(), entityId.getInternalKey()))
				.thenReturn(testDataset.getImages());
	}

	/**
	 * Tests that the addImage method correctly adds the image list.
	 * @throws Exception
	 */
	@Test
	public void testAddImages() throws Exception {
		final Dataset dataset = new Dataset();
		// shallow copy
		BeanUtils.copyProperties(testDataset, dataset);
		// remove images
		Field images = Dataset.class.getDeclaredField("images");
		images.setAccessible(true);
		images.set(dataset, null);
		
		imageService.addImages(dataset);
		
		assertEquals(this.testDataset.getImages(), dataset.getImages());
	}
	
	/**
	 * Tests that no images are added for categories that are in the exclude list.
	 * @throws Exception
	 */
	@Test
	public void testAddImagesExcluded() throws Exception {
		final Dataset dataset = new Dataset();
		// shallow copy
		BeanUtils.copyProperties(testDataset, dataset);
		// remove images
		Field images = Dataset.class.getDeclaredField("images");
		images.setAccessible(true);
		images.set(dataset, null);
		// change entityId
		dataset.setArachneId(new EntityId("noImage", 321L, 2L, false, null));
		
		imageService.addImages(dataset);
		
		assertNull(dataset.getImages());
	}
	
	/**
	 * Tests that the single image of a 'MARBilder'-entity is added correctly.
	 * @throws Exception
	 */
	@Test
	public void testAddImagesMARBilder() throws Exception {
		final Dataset dataset = new Dataset();
		// shallow copy
		BeanUtils.copyProperties(testDataset, dataset);
		// change table name
		dataset.setArachneId(new EntityId("marbilder", 123L, 123L, false, 1L));
		// add file name field
		dataset.getFields().put("marbilder.DateinameMarbilder", "Image 123.someimageformat");
		
		imageService.addImages(dataset);
		
		List<Image> actualImages = dataset.getImages();
		
		assertEquals(1, actualImages.size());
		assertEquals(testDataset.getImages().get(0), actualImages.get(0));
	}

	/**
	 * Tests the <code>getImageSubList</code> method. 
	 */
	@Test
	public void testGetImagesSubList() {
		TypeWithHTTPStatus<List<Image>> expectedValue = new TypeWithHTTPStatus<>(
				new ArrayList<>(testDataset.getImages().subList(1, 3)));   
		TypeWithHTTPStatus<List<Image>> actualValue = imageService.getImagesSubList(entityId, 1, 2);
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Tests that the method works for limits exceeding the image list size.
	 */
	@Test
	public void testGetImagesSubListLimitTooLarge() {
		TypeWithHTTPStatus<List<Image>> expectedValue = new TypeWithHTTPStatus<>(
				new ArrayList<>(testDataset.getImages().subList(2, 4)));   
		TypeWithHTTPStatus<List<Image>> actualValue = imageService.getImagesSubList(entityId, 2, 12345);
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Tests that negative offsets are handled correctly.
	 */
	@Test
	public void testGetImagesSubListInvalidNegativOffset() {
		TypeWithHTTPStatus<List<Image>> actualValue = imageService.getImagesSubList(entityId, -2, 0);
		
		assertEquals(HttpStatus.BAD_REQUEST, actualValue.getStatus());
	}
	
	/**
	 * Tests that negative limits are handled correctly.
	 */
	@Test
	public void testGetImagesSubListInvalidNegativLimit() {
		TypeWithHTTPStatus<List<Image>> actualValue = imageService.getImagesSubList(entityId, 0, -2);
		
		assertEquals(HttpStatus.BAD_REQUEST, actualValue.getStatus());
	}
	
	/**
	 * Tests the correct return value for categories that are in the exclude list. 
	 */
	@Test
	public void testGetImagesSubListInvalidExcluded() {
		EntityId entityId = new EntityId("noImage", 321L, 2L, false, null);
		TypeWithHTTPStatus<List<Image>> actualValue = imageService.getImagesSubList(entityId, 0, 2);
		
		assertEquals(HttpStatus.NOT_FOUND, actualValue.getStatus());
	}
	
	/**
	 * Tests that no images are returned if the entity itself is an image. 
	 */
	@Test
	public void testGetImagesSubListInvalidMARBilder() {
		EntityId entityId = new EntityId("marbilder", 321L, 2L, false, null);
		TypeWithHTTPStatus<List<Image>> actualValue = imageService.getImagesSubList(entityId, 0, 2);
		
		assertEquals(HttpStatus.NOT_FOUND, actualValue.getStatus());
	}
}
