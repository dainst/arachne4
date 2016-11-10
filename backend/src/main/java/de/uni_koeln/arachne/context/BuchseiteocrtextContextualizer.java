package de.uni_koeln.arachne.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import de.uni_koeln.arachne.response.AdditionalContent;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.network.ArachneRestTemplate;

/**
 * Contextualizer to retrieve OCR texts of book pages from XELETOR and add it to the corresponding dataset.
 * 
 * @author David Neugebauer
 * @author Reimar Grabowski
 */
public class BuchseiteocrtextContextualizer extends AbstractContextualizer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuchseiteocrtextContextualizer.class);
	
	private final static String XELETOR_ADRESS = "http://arachne.uni-koeln.de:6688"; 
	
	private final static String CONTEXT_TYPE = "Buchseiteocrtext";
	
	private transient final static ArachneRestTemplate restTemplate = new ArachneRestTemplate(1000, 1000);

	/**
	 * Retrieves OCR text for a book page by querying the xeletor xml server, then adds all
	 * text content to the dataset as <code>AdditionalContent</code>
	 * @return always null, because we are not building actual contexts but only
	 * 			adding a custom field to <code>Dataset</code> parent.
	 * @param parent the dataset of the buchseite, that the pages text will be added to.
	 */
	public List<AbstractLink> retrieve(final Dataset parent) {
		if (rightsService.isDataimporter()) {
			final List<Map<String, String>> bookDataList = genericSQLDao.getConnectedEntities("buch", parent.getArachneId().getArachneEntityID());
			Map<String, String> bookData = null;
			if (bookDataList != null && !bookDataList.isEmpty()) {
				bookData = bookDataList.get(0);
			}
			if ((bookData != null) && ("1".equals(bookData.get("buch.hasOcrText")))) {
				final String directoryName = bookData.get("buch.Verzeichnis");
				if (directoryName != null && !(directoryName.isEmpty())) {
					final String ocrTextAsXML = retrieveTextAsXML(directoryName, parent.getArachneId().getInternalKey());
					final String ocrText = getTextContentFromXMLString(ocrTextAsXML);
					if (!StrUtils.isEmptyOrNull(ocrText)) {
						final AdditionalContent additionalContent = new AdditionalContent();
						additionalContent.setOcrText(ocrText);
						parent.setAdditionalContent(additionalContent);	
					}
				} else {
					LOGGER.error("Field buch.Verzeichnis is not properly defined for conected entity: " + parent.getArachneId().getArachneEntityID());
				}
			}
		}
		return null;
	}
	
	/**
	 * Queries the xeletor xml server using the supplied directoryname and the 
	 * internal key of the page. Returns xeletors response as an XML-Snippet
	 * @param directory		the 'Verzeichnis' key associated with the pages book
	 * @param internalKey	the internal key of the page in table 'buchseite'
	 * @return				the pages ocr-Text as XML-String
	 */
	private String retrieveTextAsXML(final String directory, final long internalKey) {
		String result = "";
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		try {
			final URL serverAdress = new URL(XELETOR_ADRESS 
					+ "/findnodes?doc(arachne/" 
					+ directory 
					+ "/transcription.xml)TEI/text/body/div[@xml:id='p"
					+ internalKey
					+ "']");
						
			final ResponseEntity<String> response = restTemplate.exchange(serverAdress.toURI(), HttpMethod.GET, entity
					, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				return response.getBody();
			} else {
				LOGGER.warn("There was a problem contacting XELETOR. OCR text is not available. Http status code: " + response.getStatusCode());
			}	
		} catch (RestClientException | MalformedURLException | URISyntaxException e) {
			LOGGER.warn("There was a problem contacting XELETOR. OCR text is not available. Cause: ", e);
		}
		return result;
	}
	
	private String getTextContentFromXMLString(final String inputString) {
		if (!StrUtils.isEmptyOrNull(inputString)) {
			StringBuilder stringBuilder = new StringBuilder();
			SAXBuilder xmlParser = xmlConfigUtil.getXMLParser();
			ByteArrayInputStream bis = new ByteArrayInputStream(inputString.getBytes());
			try {
				Document doc = (Document) xmlParser.build(bis);
				Iterator<Element> elems = doc.getDescendants(new ElementFilter());
				while (elems.hasNext()) {
					Element elem = (Element) elems.next();
					stringBuilder.append(elem.getTextNormalize());
				}
			} catch (JDOMException e) {
				LOGGER.error(e.getMessage());
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
			return stringBuilder.toString();
		}
		return null;
	}
	
	/**
	 * @return the contextType
	 */
	public String getContextType() {
		return CONTEXT_TYPE;
	}

}
