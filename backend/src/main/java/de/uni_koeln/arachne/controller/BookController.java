package de.uni_koeln.arachne.controller;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.uni_koeln.arachne.dao.jdbc.BookDao;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;


/**
 * @author: Daniel de Oliveira
 */
@Controller
public class BookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    private static final String RESPONSE_MSG_NOT_FOUND = "Resource not found.";

    private String booksPath = null;

    @Autowired
    private transient BookDao bookDao;


    /**
     * Used to indicate when TEI xml is not formed as expected.
     */
    private class TEIException extends Exception {
        private static final long serialVersionUID = 1L;

		public TEIException(String message) {
            super("TEI not well-formed: "+message);
        }
    }


    private File teiFile(String bookId){
        return new File(booksPath+bookId+"/transcription.xml");
    }

    /**
     * Takes the TEI xml file at booksPath/{bookId}/transcription.xml
     * and renders selected information to a json format which is exemplified by:
     * {
     *     pages : [
     *       {
     *           img_file : "img_file_url" TODO discuss naming of img_file
     *       }, ...
     *     ]
     * }
     *
     * Status code is
     *   404 if TEI document for bookId is not found
     *   200 if json could be created successfully
     *   500 if an error occurred during the xml to json transformation
     *
     */
    @RequestMapping(value="/book/{arachneEntityId}",
            method=RequestMethod.GET,
            produces = {APPLICATION_JSON_UTF8_VALUE})
    public @ResponseBody ResponseEntity<String> handleGetEntityIdRequest(
            @PathVariable("arachneEntityId") final String arachneEntityId,
            final HttpServletResponse response) {

        if (booksPath==null) throw new IllegalStateException("bookPath must not be null");
        if (bookDao==null)   throw new IllegalStateException("bookDao must not be null");


        String teiFolderName=bookDao.getTEIFolderName(arachneEntityId);
        if (teiFolderName==null){
            LOGGER.error("bookId could not be determined for arachneEntityId: "+arachneEntityId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RESPONSE_MSG_NOT_FOUND);
        }

        if (!teiFile(teiFolderName).exists()){
            LOGGER.error("File not found: "+teiFile(teiFolderName));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RESPONSE_MSG_NOT_FOUND);
        }

        try {
            return ResponseEntity.status(HttpStatus.OK).body(buildJsonFromTeiTranscript(teiFile(teiFolderName)));
        } catch (Exception e){
            LOGGER.error("An error occured while parsing "+teiFile(teiFolderName)+" -> "+e.getClass() + ":" + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RESPONSE_MSG_NOT_FOUND) ;
        }
    }



    /**
     * @param xmlFile
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws TEIException
     */
    private String buildJsonFromTeiTranscript(File xmlFile) throws JDOMException, IOException, TEIException {

        SAXBuilder builder = new SAXBuilder();

        Element facsimileNode=null;
        try {
            facsimileNode = ((Document) builder.build(xmlFile)).getRootElement().getChildren().get(1);
        } catch (IndexOutOfBoundsException iex){
            throw new TEIException("Missing header element.");
        }

        return convertSurfaceElementsToJson(
                facsimileNode.getChildren(),
                facsimileNode.getAttributeValue("base", Namespace.XML_NAMESPACE));
    }

    /**
     *
     * @param listOfSurfaceElements
     * @param img_filePrefix
     * @return
     */
    private String convertSurfaceElementsToJson(List<?> listOfSurfaceElements, String img_filePrefix)
            throws IOException {

        StringWriter sw = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(sw);

        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("pages");
        jsonGenerator.writeStartArray();

        for (Object surfaceEl : listOfSurfaceElements){

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("img_file",img_filePrefix+"/"+
                    ((Element) surfaceEl).getChildren().get(0).getAttributeValue("url"));
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.close();


        String result = sw.toString();
        sw.close();
        return result;
    }

    /**
     * @param booksPath path to the folder where the TEI files are stored
     */
    @Value("${booksPath}")
    public void setBooksPath(String booksPath){
        if (!new File(booksPath).exists()) throw new IllegalArgumentException("Must exist: "+booksPath);

        this.booksPath = booksPath;
        if (!booksPath.endsWith("/")) this.booksPath+="/";
    }

    /**
     * @param bookDao
     */
    public void setBookDao(BookDao bookDao){
        this.bookDao = bookDao;
    }
}
