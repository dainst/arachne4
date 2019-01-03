package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.*;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestExport {

    @InjectMocks
    private transient DataExportStack monoStack = new DataExportStack(2, 1, 100);

    @InjectMocks
    private transient DataExportStack duoStack = new DataExportStack(4, 2, 100);

    @Mock
    private IIPService iipService;

    @Mock
    private Transl8Service transl8Service;

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
    private MailService mailService;

    @Mock
    private RequestAttributes requestAttributes;

    @Mock
    private ThreadPoolTaskExecutor taskExecutor;

    @Mock
    private User user;

    @Mock
    private User otherUser;

    @Mock
    private DataExportFileManager dataExportFileManager;

    @Mock
    private DataExportConversionObject genericConversionObject;

    private ServletContext servletContext = new MockServletContext("file:src/main/webapp");

    private boolean switchIsLoggedIn = true;

    private class genericExportConverter extends AbstractDataExportConverter {

        genericExportConverter() {
            super(MediaType.TEXT_PLAIN);
        }

        @Override
        void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {

        }

        @Override
        protected boolean supports(Class aClass) {
            return true;
        }

        @Override
        protected void writeInternal(Object o, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {

        }
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

    private DataExportTask createGenericTask() {
        final genericExportConverter converter = new genericExportConverter();
        prepareConverter(converter, genericConversionObject);
        return converter.task;
    }

    @Before
    public void setUp() throws Transl8Service.Transl8Exception, IOException {

        // request
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // test data
        final String json = new String(TestData.getTestJson(), StandardCharsets.UTF_8);

        // mocked services
        when(transl8Service.transl8(eq("date_format"), anyString()))
                .thenReturn("dd.MM.yyyy HH:mm:ss");
        when(transl8Service.transl8(anyString(), anyString()))
                .thenAnswer((Answer<String>) invocation -> "transl8ed: " + invocation.getArguments()[0]);

        when(entityService.getEntityFromIndex(anyLong(), anyString(), anyString()))
                .thenReturn(new TypeWithHTTPStatus<String>(json));
        when(entityService.getEntityFromDB(anyLong(), anyString(), anyString()))
                .thenReturn(new TypeWithHTTPStatus<String>(json));

        when(userRightsService.isSignedInUser())
                .thenAnswer((Answer<Boolean>) invocationOnMock -> switchIsLoggedIn);

        when(dataExportFileManager.getFileUrl(any()))
                .thenReturn("URL");

        // mocked objects
        when(genericConversionObject.getType()).thenReturn("");
        when(user.getFirstname()).thenReturn("test");
        when(user.getLastname()).thenReturn("test");
        when(user.getEmail()).thenReturn("test@test.de");
        when(user.getId()).thenReturn((long) 1);
        when(otherUser.getId()).thenReturn((long) 2);

        // multi-threading  (from https://dzone.com/articles/workaround-multi-threaded)
        doAnswer((Answer<Object>) invocation -> {
            Object[] args = invocation.getArguments();
            Runnable runnable = (Runnable)args[0];
            runnable.run();
            return null;
        }).when(taskExecutor).submit(any(Runnable.class));

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

    private HashMap<String, Integer> analyzeStatus(JSONObject fullStatus) {

        final JSONObject tasks = fullStatus.getJSONObject("tasks");
        final Iterator<String> taskIds = tasks.keys();
        final HashMap<String, Integer> taskStatusCounter = new HashMap<String, Integer>();

        while (taskIds.hasNext()) {
            String taskId = taskIds.next();
            String thisStatus = tasks.getJSONObject(taskId).getString("status");
            if (!taskStatusCounter.containsKey(thisStatus)) {
                taskStatusCounter.put(thisStatus, 1);
            } else {
                taskStatusCounter.replace(thisStatus, taskStatusCounter.get(thisStatus) + 1);
            }

        }

        System.out.println("----- status -----");
        taskStatusCounter.forEach((key, value) -> {
            System.out.println(key + " : " + value);
        });

        return taskStatusCounter;
    }


    @Test
    public void testStack() throws Exception {

        // 1 fill stack until full and reject if full
        // user jedöns
        // 2 test enqueue and dequeue
        // 3 set stack to one at a time and see how task come one after each other
        // test abort
        // 4 see tasks in parallel

        HashMap<String, Integer> stackStatus;

        // 1. add some tasks to stack and start the first
        final DataExportTask task1 = createGenericTask();
        final DataExportTask task2 = createGenericTask();
        final DataExportTask task3 = createGenericTask();
        monoStack.push(task1);
        monoStack.push(task2);
        monoStack.push(task3);

        stackStatus = analyzeStatus(monoStack.getStatus(user));
        assertEquals((int) stackStatus.get("enqueued"), 2);
        assertEquals((int) stackStatus.get("running"), 1);

        // 2. throw exception if stack is full
        try {
            monoStack.push(createGenericTask());
            fail("Expected DataExportException.");
        } catch (DataExportException expected) {
            assertEquals("error_data_export_stack_full", expected.getMessage());
        }
        stackStatus = analyzeStatus(monoStack.getStatus(user));
        assertEquals((int) stackStatus.get("enqueued"), 2);
        assertEquals((int) stackStatus.get("running"), 1);

        // 3. finish one task, start the next and make room for more
        monoStack.taskIsFinishedListener(task1);
        stackStatus = analyzeStatus(monoStack.getStatus(user));
        assertEquals((int) stackStatus.get("enqueued"), 1);
        assertEquals((int) stackStatus.get("running"), 1);
        assertEquals((int) stackStatus.get("finished"), 1);

        // 4. task throwing error
        task3.error = "error";
        monoStack.taskIsFinishedListener(task3);
        stackStatus = analyzeStatus(monoStack.getStatus(user));
        assertFalse(stackStatus.containsKey("enqueued"));
        assertEquals((int) stackStatus.get("running"), 1);
        assertEquals((int) stackStatus.get("finished"), 1);
        assertEquals((int) stackStatus.get("error"), 1);

        // 5. only see owned tasks
        stackStatus = analyzeStatus(monoStack.getStatus(otherUser));
        assertFalse(stackStatus.containsKey("enqueued"));
        assertFalse(stackStatus.containsKey("running"));
        assertFalse(stackStatus.containsKey("finished"));
        assertFalse(stackStatus.containsKey("error"));


    }


}
