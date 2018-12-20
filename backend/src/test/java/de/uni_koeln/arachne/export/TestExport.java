package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.response.ResponseFactory;
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
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

        final String json = new String(TestData.getTestJson(), StandardCharsets.UTF_8);

        when(entityService.getEntityFromIndex(anyLong(), anyString(), anyString())).thenReturn(new TypeWithHTTPStatus<String>(json));
        when(entityService.getEntityFromDB(anyLong(), anyString(), anyString())).thenReturn(new TypeWithHTTPStatus<String>(json));
    }

    private void prepareConverter(AbstractDataExportConverter converter, DataExportConversionObject conversion) {

        converter.injectService(transl8Service);

        converter.injectService(servletContext);

        converter.injectService(iipService);

        converter.injectService(catalogEntryDao);

        converter.injectService(entityService);

        final DataExportTask task = new DataExportTask(converter, conversion);
        task.setOwner(user);
        task.setRequestUrl("http://arachne.dainst.mock-request");
        task.setBackendUrl("http://arachne.dainst.org/data");
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

        final String[] lines = out.toString().split(System.lineSeparator());

        assertEquals(lines[0].replaceAll("\\p{C}", ""), TestData.exportCsvLine1);
        assertEquals(lines[1].replaceAll("\\p{C}", ""), TestData.exportCsvLine2);
        assertEquals(lines[2].replaceAll("\\p{C}", ""), TestData.exportCsvLine3);
    }


}
