package de.uni_koeln.arachne.book.controller;
import de.uni_koeln.arachne.book.dao.BookDAO;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * @author: Daniel M. de Oliveira
 */
@Controller
public class BookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    private String booksPath = null;

    @Autowired
    private transient BookDAO bookDao;


    /**
     * Used to indicate when TEI xml is not formed as expected.
     */
    private class TEIException extends Exception {
        public TEIException(String message) {
            super("TEI not well-formed: "+message);
        }
    }

    /**
     *
     */
    private class Response {

        public Integer status;
        public String  body;
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
    @RequestMapping(value="/book/{arachneEntityId}", method=RequestMethod.GET)
    public @ResponseBody ResponseEntity<String> handleGetEntityIdRequest(
            @PathVariable("arachneEntityId") final String arachneEntityId,
            final HttpServletResponse response) {

        if (booksPath==null) throw new IllegalStateException("bookPath must not be null");
        if (bookDao==null) throw new IllegalStateException("bookDao must not be null");


        String bookId=bookDao.getTEIFolderName(arachneEntityId);
        if (bookId==null)
            return ResponseEntity.status(404).body("bookId was null"); // TODO choose proper return code and msg

        File xmlFile = new File(booksPath+bookId+"/transcription.xml");
        if (!xmlFile.exists())
            return ResponseEntity.status(404).body("Not found: "+xmlFile);

        Response resp= handleSuccessAndErrorOnXmlTeiParsing(xmlFile);
        return ResponseEntity.status(resp.status).body(resp.body);
    }


    /**
     * @param xmlFile an existing file which is assumed to be a valid and well-formed XML / TEI file
     *
     * @return <ul><li>a response object with a status of 500
     *   and an error msg if something went wrong during parsing of xmlFile.
     *   </li><li>a response object with a status of 200 and a body with the
     *   JSON string generated during parsing xmlFile</li></ul>
     */
    private Response handleSuccessAndErrorOnXmlTeiParsing(File xmlFile){
        Response resp = new Response();
        resp.body = "";
        resp.status = 500;
        try {
            resp.body = teiTranscriptToJson(xmlFile);
            resp.status = 200;
        } catch (IOException io) {
            resp.body=io.getMessage();
        } catch (JDOMException jdomex){
            resp.body=jdomex.getMessage();
        } catch (TEIException teie) {
            resp.body=teie.getMessage();
        }
        return resp;
    }


    /**
     * @param xmlFile
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws TEIException
     */
    private String teiTranscriptToJson(File xmlFile) throws JDOMException, IOException, TEIException {

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
    private String convertSurfaceElementsToJson(List listOfSurfaceElements, String img_filePrefix){
        StringBuilder resultStringBuilder = new StringBuilder();
        resultStringBuilder.append("{\n\t\"pages\":[\n");

        for (int i=0;i< listOfSurfaceElements.size();i++){
            resultStringBuilder.append("\t{\n");
            resultStringBuilder.append("\t\t\"img_file\":\""+img_filePrefix+"/"+
                    ((Element) listOfSurfaceElements.get(i)).getChildren().get(0).getAttributeValue("url")+"\"\n\t}");
            if (i!=listOfSurfaceElements.size()-1) resultStringBuilder.append(",");
            resultStringBuilder.append("\n");
        }

        resultStringBuilder.append("\t]\n}");
        return resultStringBuilder.toString();
    }

    /**
     * @param booksPath
     */
    @Value("${booksPath}") // Path to the folder where the TEI files are stored
    public void setBooksPath(String booksPath){
        if (!new File(booksPath).exists()) throw new IllegalArgumentException("Must exist: "+booksPath);

        this.booksPath = booksPath;
        if (!booksPath.endsWith("/")) this.booksPath+="/";
    }

    /**
     * @param bookDao
     */
    public void setBookDao(BookDAO bookDao){
        this.bookDao = bookDao;
    }
}
