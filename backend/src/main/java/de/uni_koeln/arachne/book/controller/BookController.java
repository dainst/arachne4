package de.uni_koeln.arachne.book.controller;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Used to indicate when TEI xml is not formed as expected.
     */
    private class TEIException extends Exception {
        public TEIException(String message) {
            super("TEI not well-formed: "+message);
        }
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
    @RequestMapping(value="/book/{bookId}", method=RequestMethod.GET)

    public @ResponseBody ResponseEntity<String> handleGetEntityIdRequest(
            @PathVariable("bookId") final String bookId,
            final HttpServletResponse response) {

        if (booksPath==null) throw new IllegalStateException("bookPath must not be null");

        File xmlFile = new File(booksPath+bookId+"/transcription.xml");
        if (!xmlFile.exists())
            return ResponseEntity.status(404).body("Not found: "+xmlFile);

        String resultString = "";
        Integer resultStatus = 500;
        try {
            resultString = teiTranscriptToJson(xmlFile);
            resultStatus = 200;
        } catch (IOException io) {
            resultString=io.getMessage();
        } catch (JDOMException jdomex){
            resultString=jdomex.getMessage();
        } catch (TEIException teie) {
            resultString=teie.getMessage();
        }

        return ResponseEntity.status(resultStatus).body(resultString);
    }


    private String teiTranscriptToJson(File xmlFile) throws JDOMException, IOException, TEIException {

        SAXBuilder builder = new SAXBuilder();
        Document document = (Document) builder.build(xmlFile);

        Element facsimileNode=null;
        try {
            facsimileNode = document.getRootElement().getChildren().get(1);
        } catch (IndexOutOfBoundsException iex){
            throw new TEIException("Missing header element.");
        }

        return convertSurfaceElementsToJson(
                facsimileNode.getChildren(),
                facsimileNode.getAttributeValue("base", Namespace.XML_NAMESPACE));
    }

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

    @Value("${booksPath}") // Path to the folder where the TEI files are stored
    public void setBooksPath(String booksPath){
        if (!new File(booksPath).exists()) throw new IllegalArgumentException("Must exist: "+booksPath);

        this.booksPath = booksPath;
        if (!booksPath.endsWith("/")) this.booksPath+="/";
    }
}
