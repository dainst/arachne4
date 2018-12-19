package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.ArachneEntity;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.*;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestExport {

    @Mock
    private IIPService iipService;

    @Mock
    private Transl8Service transl8Service;

    private ServletContext servletContext = new MockServletContext("file:src/main/webapp");

    @Mock
    private CatalogEntryDao catalogEntryDao;

    @Mock
    private EntityService entityService;

    @Mock
    private UserRightsService userRightsService;

    @Mock
    private SingleEntityDataService singleEntityDataService;

    @Mock
    private ResponseFactory responseFactory;

    @Mock
    private EntityIdentificationService entityIdentificationService;

    @Mock
    private ESService esService;

    @Mock
    private User user;

    private transient EntityId testId;

    private final String LANG = "de";

    @Before
    public void setUp() throws Transl8Service.Transl8Exception, IOException {
        when(transl8Service.transl8(eq("date_format"), anyString())).thenReturn("dd.MM.yyyy HH:mm:ss");

//        SearchResult sr = TestData.getDefaultSearchResult();
//        List<SearchHit> searchHits = sr.getEntities();
//        List<ArachneEntity> entities = searchHits.stream().map((SearchHit hit) -> new ArachneEntity()).collect(Collectors.toList());

        final String json = new String(TestData.getTestJson(), StandardCharsets.UTF_8);

        System.out.println("\n");
        System.out.println(json);
        System.out.println("\n");

        when(entityService.getEntityFromIndex(anyLong(), anyString(), anyString())).thenReturn(new TypeWithHTTPStatus<String>(json));
        when(entityService.getEntityFromDB(anyLong(), anyString(), anyString())).thenReturn(new TypeWithHTTPStatus<String>(json));

//        final Dataset testDataset = TestData.getTestDataset();
//
//        when(userRightsService.isDataimporter()).thenReturn(false, false, true);
//        when(userRightsService.userHasDatasetGroup(null)).thenReturn(false, true);
//
//        testId = TestData.getTestDataset().getArachneId();
//        when(singleEntityDataService.getSingleEntityByArachneId(testId)).thenReturn(testDataset);
//
//        when(responseFactory.createFormattedArachneEntityAsJsonString(testDataset, LANG))
//                .thenReturn(TestData.jsonString);
//        when(responseFactory.createFormattedArachneEntityAsJson(testDataset, LANG))
//                .thenReturn(TestData.getTestJson());
//
//        when(entityIdentificationService.getId(anyLong())).thenReturn(null);
//        when(entityIdentificationService.getId(anyString(), anyLong())).thenReturn(null);
//        when(entityIdentificationService.getId(0l)).thenReturn(testId);
//        when(entityIdentificationService.getId(2l)).thenReturn(TestData.deletedEntity);
//        when(entityIdentificationService.getId("test", 0l)).thenReturn(testId);
//
//        when(esService.getDocumentFromCurrentIndex(anyLong(), anyString(), any(String[].class), anyString()))
//                .thenReturn(new TypeWithHTTPStatus<String>(null, HttpStatus.NOT_FOUND));
//
//        when(esService.getDocumentFromCurrentIndex(0l, null, new String[] {"boost","connectedEntities","degree","fields"}, LANG))
//                .thenReturn(new TypeWithHTTPStatus<String>(HttpStatus.FORBIDDEN), new TypeWithHTTPStatus<String>("Test Doc", HttpStatus.OK));
//
//        when(esService.getDocumentFromCurrentIndex(0l, "test", new String[] {"boost","connectedEntities","degree","fields"}, LANG))
//                .thenReturn(new TypeWithHTTPStatus<String>("Test Doc", HttpStatus.OK));

    }

    private void prepareConverter(AbstractDataExportConverter converter, DataExportConversionObject conversion) {
        converter.injectService(entityService);
        converter.injectService(transl8Service);
        converter.injectService(servletContext);
        converter.injectService(iipService);
        converter.injectService(catalogEntryDao);



        final DataExportTask task = new DataExportTask(converter, conversion);
        task.setOwner(user);
        task.setRequestUrl("x");
        task.setBackendUrl("y");
        task.setUserRightsService(userRightsService);
        task.setLanguage("DE");
        converter.task = task;
    }

    @Test
    public void testSearchResultToCsvExport() throws Exception {

        final SearchResult2CsvConverter converter = new SearchResult2CsvConverter();
        final SearchResult searchResult = TestData.getDefaultSearchResult();
        final DataExportConversionObject conversion = new DataExportConversionObject(searchResult);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        prepareConverter(converter, conversion);
        converter.convert(conversion, out);
        System.out.println(out.toString());

    }


}
