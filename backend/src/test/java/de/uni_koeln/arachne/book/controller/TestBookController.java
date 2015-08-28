package de.uni_koeln.arachne.book.controller;

import static java.util.regex.Pattern.*;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.regex.Pattern;

/**
 * @author: Daniel M. de Oliveira
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBookController {

    private static final String BOOKSPATH="src/test/resources/book/controller/BookController/"; // should contain ending slash

    @InjectMocks
    private BookController controller;

    private MockMvc mockMvc;

    @Before
    public void setUp(){

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        controller.setBooksPath(BOOKSPATH);
    }


    // TODO replace by java path class.
    @Test
    public void acceptBookPathWithoutEndingSlash() throws Exception {

        controller.setBooksPath(BOOKSPATH.substring(0, BOOKSPATH.length() - 1));
        mockMvc.perform(
                get("/book/aoi")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    // TODO discuss. questionable if application should not start in this case.
    @Test
    public void invalidBookPath() {
        try {
            controller.setBooksPath("/tmp/tmp/tmp/notexisting/");
            fail();
        } catch (IllegalArgumentException expected){}
    }


    @Test
    public void xmlNotFound() throws Exception {

        mockMvc.perform(
                get("/book/aoi_not_found")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }


    @Test
    public void teiNotWellFormedMissingHeader() throws Exception {

        MvcResult result = mockMvc.perform(
                get("/book/aoi_ill_formed")
                        .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isInternalServerError())
                .andReturn();

        assertEquals("TEI not well-formed: Missing header element.", result.getResponse().getContentAsString());

    }


    @Test
    public void convertBookFromXMLtoJSON() throws Exception {

        MvcResult result = mockMvc.perform(
                get("/book/aoi")
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
