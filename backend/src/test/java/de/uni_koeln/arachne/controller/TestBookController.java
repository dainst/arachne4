package de.uni_koeln.arachne.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;

import de.uni_koeln.arachne.dao.jdbc.BookDao;
import de.uni_koeln.arachne.controller.BookController;
import de.uni_koeln.arachne.util.TestJSONUtil;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author: Daniel M. de Oliveira
 */
@ExtendWith(MockitoExtension.class)
public class TestBookController {

        private static final String BOOKSPATH = "src/test/resources/controller/BookController/"; // should contain
                                                                                                 // ending slash

        @InjectMocks
        private BookController controller;

        private MockMvc mockMvc;

        private BookDao mockBockDao = mock(BookDao.class);

        @BeforeEach
        public void setUp() {

                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
                controller.setBooksPath(BOOKSPATH);
                controller.setBookDao(mockBockDao);
        }

        // TODO replace by java path class.
        /* ~~(org/openrewrite/staticanalysis/LambdaBlockToExpression)~~> */@Test
        public void acceptBookPathWithoutEndingSlash() throws Exception {

                controller.setBooksPath(BOOKSPATH.substring(0, BOOKSPATH.length() - 1));
                when(mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi");
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
                } catch (IllegalArgumentException expected) {
                }
        }

        @Test
        public void entityNoBookIdForEntityID() throws Exception {
                when(mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn(null);
                mockMvc.perform(
                                get("/book/arachneEntityId")
                                                .contentType(APPLICATION_JSON_UTF8))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void xmlNotFound() throws Exception {
                when(mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi_not_found");
                mockMvc.perform(
                                get("/book/arachneEntityId")
                                                .contentType(APPLICATION_JSON_UTF8))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void teiNotWellFormedMissingHeader() throws Exception {

                when(mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi_ill_formed");
                mockMvc.perform(
                                get("/book/arachneEntityId")
                                                .contentType(APPLICATION_JSON_UTF8))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void convertBookFromXMLtoJSON() throws Exception {

                when(mockBockDao.getTEIFolderName("arachneEntityId")).thenReturn("aoi");
                MvcResult result = mockMvc.perform(
                                get("/book/arachneEntityId")
                                                .contentType(APPLICATION_JSON_UTF8))
                                .andExpect(status().isOk())
                                .andReturn();

                String content = result.getResponse().getContentAsString();

                final String toBe = "{pages:["
                                + "{img_file: \"http://arachne.uni-koeln.de/images/stichwerke/antiquities_of_ionia_1/BOOK-antiquitiesofionia01-0001_196.jpg\"}"
                                +
                                ",{img_file:\"http://arachne.uni-koeln.de/images/stichwerke/antiquities_of_ionia_1/BOOK-antiquitiesofionia01-0002_197.jpg\"}]}";

                assertTrue(TestJSONUtil.areEqual(new JSONObject(content), new JSONObject(toBe)));

        }
}
