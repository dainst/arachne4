package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.*;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import java.io.*;
import java.nio.charset.Charset;
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
    private transient DataExportStack monoStack = new DataExportStack(2, 1, 100, "arachne.dainst.mock");

    @InjectMocks
    private transient DataExportStack duoStack = new DataExportStack(2, 2, 100, "arachne.dainst.mock");

    @InjectMocks
    private transient DataExportFileManager fileManager = new DataExportFileManager("/tmp");

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

    private class genericExportConverter extends AbstractDataExportConverter<Object> {

        genericExportConverter() {
            super(MediaType.TEXT_PLAIN);
        }

        @Override
        void convert(DataExportConversionObject conversionObject, OutputStream outputStream) throws IOException {
            outputStream.write("test-conversion-result".getBytes());
        }

        @Override
        protected boolean supports(Class<?> aClass) {
            return true;
        }

        @Override
        protected void writeInternal(Object o, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {

        }
    }

    private void prepareConverter(AbstractDataExportConverter<?> converter, DataExportConversionObject conversion) {

        converter.injectService(transl8Service);
        converter.injectService(servletContext);
        converter.injectService(iipService);
        converter.injectService(catalogEntryDao);
        converter.injectService(entityService);

        final DataExportTask task = new DataExportTask(converter, conversion);
        task.setOwner(user);
        task.setRequestUrl("http://arachne.dainst.mock-request?q=test");
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

    private HashMap<String, Integer> analyzeStatus(JSONObject fullStatus) {

        final JSONObject tasks = fullStatus.getJSONObject("tasks");
        final Iterator<String> taskIds = tasks.keys();
        final HashMap<String, Integer> taskStatusCounter = new HashMap<>();

        while (taskIds.hasNext()) {
            String taskId = taskIds.next();
            String thisStatus = tasks.getJSONObject(taskId).getString("status");
            if (!taskStatusCounter.containsKey(thisStatus)) {
                taskStatusCounter.put(thisStatus, 1);
            } else {
                taskStatusCounter.replace(thisStatus, taskStatusCounter.get(thisStatus) + 1);
            }

        }

        // System.out.println("----- status -----");
        // taskStatusCounter.forEach((key, value) -> {System.out.println(key + " : " + value);});

        return taskStatusCounter;
    }

    private String convert(AbstractDataExportConverter<?> converter, DataExportConversionObject conversion) throws IOException {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        prepareConverter(converter, conversion);
        converter.convert(conversion, out);

        return out.toString();
    }

    private String[] parseLines(String str) {

        final String[] lines = str.split(System.lineSeparator());

        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replaceAll("\\p{C}", "");
        }

        return lines;
    }

    @Before
    public void setUp() throws Transl8Service.Transl8Exception, IOException {

        // request
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // test data
        final String json = new String(TestData.getTestJson(), StandardCharsets.UTF_8);

        // mocked services
        when(transl8Service.transl8(anyString(), anyString()))
                .thenAnswer((Answer<String>) invocation -> "transl8ed: " + invocation.getArguments()[0]);
        when(transl8Service.transl8(eq("date_format"), anyString()))
                .thenReturn("dd.MM.yyyy HH:mm:ss");

        when(entityService.getEntityFromIndex(anyLong(), anyString(), anyString()))
                .thenReturn(new TypeWithHTTPStatus<>(json));
        when(entityService.getEntityFromDB(anyLong(), anyString(), anyString()))
                .thenReturn(new TypeWithHTTPStatus<>(json));

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

        final String[] lines = parseLines(convert(converter, conversion));

        assertEquals(TestData.exportSearchResult2CsvLine1, lines[0]);
        assertEquals(TestData.exportSearchResult2CsvLine2, lines[1]);
        assertEquals(TestData.exportSearchResult2CsvLine3, lines[2]);
    }

    @Test
    public void testCatalogToCsvExport() throws Exception {

        final Catalog2CsvConverter converter = new Catalog2CsvConverter();
        final Catalog catalog = TestData.getFinishedTestCatalog();
        final DataExportConversionObject conversion = new DataExportConversionObject(catalog);

        final String[] lines = parseLines(convert(converter, conversion));

        assertEquals(TestData.exportCatalog2CsvLine1, lines[0]);
        assertEquals(TestData.exportCatalog2CsvLine2, lines[1]);

    }

    @Test
    public void testSearchResultToHtmlExport() throws Exception {

        final SearchResult2HtmlConverter converter = new SearchResult2HtmlConverter();
        final SearchResult searchResult = TestData.getDefaultSearchResult();
        final DataExportConversionObject conversion = new DataExportConversionObject(searchResult);

        final Document doc = Jsoup.parse(convert(converter, conversion));

        assertEquals(4, doc.select(".page").size());

        assertEquals(
                "transl8ed: search_result_for 'test'",
                doc.select(".doc-title").first().textNodes().get(0).text()
        );
        assertEquals(
                "Test title",
                doc.select("h1.title").first().text()
        );
        assertEquals(
                10,
                doc.select(".page:nth-child(3) .dataset > tbody > tr").size()
        );
        assertEquals(
                "Subtitle of the Test",
                doc.select(".page:nth-child(3) .dataset > tbody > tr > td").first().text()
        );
        assertEquals(
                "section",
                doc.select(".page:nth-child(3) .dataset > tbody > tr > td").first().attr("class")
        );
        assertEquals(
                "data:image/png;base64,iVBORw",
                doc.select("img.logo-img").first().attr("src").substring(0, 28)
        );
    }


    @Test
    public void testCatalogToHtmlExport() throws Exception {

        final Catalog2HtmlConverter converter = new Catalog2HtmlConverter();
        final Catalog catalog = TestData.getFinishedTestCatalog();
        final DataExportConversionObject conversion = new DataExportConversionObject(catalog);

        final Document doc = Jsoup.parse(convert(converter, conversion));

        assertEquals(5, doc.select(".page").size());

        assertEquals(
                "label: root",
                doc.select(".doc-title").first().textNodes().get(0).text()
        );
        assertEquals(
                "*label*: text entry nr: 1",
                doc.select("h1.title").first().text()
        );
        assertEquals(
                "data:image/png;base64,iVBORw",
                doc.select("img.logo-img").first().attr("src").substring(0, 28)
        );
    }


    @Test
    public void testSearchResultToPdfExport() throws Exception {

        final SearchResult2PdfConverter converter = new SearchResult2PdfConverter();
        final SearchResult searchResult = TestData.getDefaultSearchResult();
        final DataExportConversionObject conversion = new DataExportConversionObject(searchResult);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        prepareConverter(converter, conversion);
        converter.convert(conversion, out);
        final byte[] pdf = out.toByteArray();

        assertEquals(3L, (long) pdfHelper.countPages(pdf));

        final String[] page1 = parseLines(pdfHelper.stripText(pdf, 1));
        final String[] page2 = parseLines(pdfHelper.stripText(pdf, 2));

        assertEquals("ARACHNE", page1[2]);
        assertEquals("http://arachne.dainst.mock-request?q=test", page1[5]);
        assertEquals("Test title", page2[1]);
        assertEquals("Subtitle of the Test", page2[2]);
        assertEquals("transl8ed: test test facet value", page2[3]);
    }


    @Test
    public void testCatalogToPdfExport() throws Exception {

        final Catalog2PdfConverter converter = new Catalog2PdfConverter();
        final Catalog catalog = TestData.getFinishedTestCatalog();
        final DataExportConversionObject conversion = new DataExportConversionObject(catalog);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        prepareConverter(converter, conversion);
        converter.convert(conversion, out);
        final byte[] pdf = out.toByteArray();

        assertEquals(4L, (long) pdfHelper.countPages(pdf));

        final String[] page1 = parseLines(pdfHelper.stripText(pdf, 1));
        final String[] page2 = parseLines(pdfHelper.stripText(pdf, 2));

        assertEquals("ARACHNE", page1[2]);
        assertEquals("label: root", page1[3]);

        assertEquals("*label*: text entry nr: 1", page2[1]);
        assertEquals("Subtitle of the Test", page2[2]);
    }


    @Test
    public void testStack() throws Exception {

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

        // 5. clean up
        monoStack.removeFinishedTask(task1);
        monoStack.removeFinishedTask(task3);
        stackStatus = analyzeStatus(monoStack.getStatus(user));
        assertFalse(stackStatus.containsKey("enqueued"));
        assertEquals((int) stackStatus.get("running"), 1);
        assertFalse(stackStatus.containsKey("finished"));
        assertFalse(stackStatus.containsKey("error"));

        // 6. only see owned tasks
        stackStatus = analyzeStatus(monoStack.getStatus(otherUser));
        assertFalse(stackStatus.containsKey("enqueued"));
        assertFalse(stackStatus.containsKey("running"));
        assertFalse(stackStatus.containsKey("finished"));
        assertFalse(stackStatus.containsKey("error"));

        // 7. work parallel
        final DataExportTask task1b = createGenericTask();
        final DataExportTask task2b = createGenericTask();
        final DataExportTask task3b = createGenericTask();
        duoStack.push(task1b);
        duoStack.push(task2b);
        duoStack.push(task3b);
        stackStatus = analyzeStatus(duoStack.getStatus(user));
        assertEquals((int) stackStatus.get("enqueued"), 1);
        assertEquals((int) stackStatus.get("running"), 2);

        // 8. dequeue tasks
        duoStack.dequeueTask(task3b);
        duoStack.abortTask(task2b);
        task2b.error = "aborted"; // we have to do it manually, since we do not use real DataExportThreads
        duoStack.taskIsFinishedListener(task2b);
        stackStatus = analyzeStatus(duoStack.getStatus(user));
        assertFalse(stackStatus.containsKey("enqueued"));
        assertEquals((int) stackStatus.get("aborted"), 2);
    }

    @Test
    public void testFileManager() throws Exception {

        final DataExportTask task = createGenericTask();
        final String name = fileManager.getFileName(task);

        final String fileContent = "testdata";

        // write testfile
        final FileOutputStream outputStream = new FileOutputStream(name);
        outputStream.write(fileContent.getBytes());
        outputStream.close();

        // get file
        final String content = IOUtils.toString(fileManager.getFile(task), Charset.defaultCharset());
        assertEquals(fileContent, content);

        // delete file
        fileManager.deleteFile(task);
        assertFalse(new File(name).exists());

        // write file with fileManager
        fileManager.writeToFile(task);
        assertTrue(new File(name).exists());
        final String content2 = IOUtils.toString(fileManager.getFile(task), Charset.defaultCharset());
        assertEquals("test-conversion-result", content2);
        fileManager.deleteFile(task);

    }
}
