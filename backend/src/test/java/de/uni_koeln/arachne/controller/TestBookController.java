package de.uni_koeln.arachne.controller;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;

import de.uni_koeln.arachne.dao.jdbc.BookDao;
import de.uni_koeln.arachne.controller.BookController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author: Daniel M. de Oliveira
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBookController {

    private static final String BOOKSPATH="src/test/resources/controller/BookController/"; // should contain ending slash

    @InjectMocks
    private BookController controller;

    private MockMvc mockMvc;

    private BookDao mockBockDao = mock(BookDao.class);

    @Before
    public void setUp(){

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        controller.setBooksPath(BOOKSPATH);
        controller.setBookDao(mockBockDao);
    }


    // TODO replace by java path class.
    @Test
    public void acceptBookPathWithoutEndingSlash() throws Exception {

        controller.setBooksPath(BOOKSPATH.substring(0, BOOKSPATH.length() - 1));
        when (mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi");
        mockMvc.perform(
                get("/book/arachneEntityId")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    public void invalidBookPath() {

        try {
            controller.setBooksPath("/tmp/tmp/tmp/notexisting/");
            fail();
        } catch (IllegalArgumentException expected){}
    }


    @Test
    public void entityNoBookIdForEntityID() throws Exception {
        when (mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn(null);
        mockMvc.perform(
                get("/book/arachneEntityId")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }


    @Test
    public void xmlNotFound() throws Exception {
        when (mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi_not_found");
        mockMvc.perform(
                get("/book/arachneEntityId")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }


    @Test
    public void teiNotWellFormedMissingHeader() throws Exception {

        when (mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi_ill_formed");
        mockMvc.perform(
                get("/book/arachneEntityId")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }


    @Test
    public void convertBookFromXMLtoJSON() throws Exception {

        when (mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi");
        MvcResult result = mockMvc.perform(
                get("/book/arachneEntityId")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        JSONAssert.assertEquals("{pages:["
                + "{img_file:\"http://arachne.uni-koeln.de/images/stichwerke/antiquities_of_ionia_1/BOOK-antiquitiesofionia01-0001_196.jpg\"}" +
                ",{img_file:\"http://arachne.uni-koeln.de/images/stichwerke/antiquities_of_ionia_1/BOOK-antiquitiesofionia01-0002_197.jpg\"}]}"
                , content, false);
    }
}
