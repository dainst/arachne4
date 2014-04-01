package de.uni_koeln.arachne.context;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

import de.uni_koeln.arachne.response.AdditionalContent;
import de.uni_koeln.arachne.response.Dataset;

public class BuchseiteocrtextContextualizer extends AbstractContextualizer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuchseiteocrtextContextualizer.class);
	
	private final static String XELETOR_ADRESS = "http://arachne.uni-koeln.de:6688"; 
	
	private final static String CONTEXT_TYPE = "Buchseiteocrtext";

	/**
	 * Retrieves ocr text for a book page by querying the xeletor xml server, then adds all
	 * text content to the dataset as <code>AdditionalContent</code>
	 * @return	always null, because we are not building actual contexts but only
	 * 			adding a custom field to <code>Dataset</code> parent.
	 * @param parent	the dataset of the buchseite, that the pages text will be added to.
	 * @param offset 	offset of the context to retrieve.
	 * @param limit		Maximum number of contexts to retireve.
	 */
	public List<AbstractLink> retrieve(final Dataset parent) {
		if (rightsService.isDataimporter()) {
			final Map<String, String> bookData = genericSQLService.getConnectedEntities("buch", parent.getArachneId().getArachneEntityID()).get(0);
			if ((bookData != null) && ("1".equals(bookData.get("buch.hasOcrText")))) {
				final String directoryName = bookData.get("buch.Verzeichnis");
				if (directoryName != null && !(directoryName.isEmpty())) {
					final String ocrTextAsXML = retrieveTextAsXML(directoryName, parent.getArachneId().getInternalKey());
					final String ocrText = getTextContentFromXMLString(ocrTextAsXML);
					if (ocrText != null && !ocrText.isEmpty()) {
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
	private String retrieveTextAsXML(final String directory, final Long internalKey) {
		HttpURLConnection connection = null;
		final URL serverAdress;
		final BufferedReader reader;
		final StringBuilder stringBuilder;
		String line = null;
		String result = null;
		try {
			serverAdress = new URL(XELETOR_ADRESS 
					+ "/findnodes?doc%28arachne/" 
					+ directory 
					+ "/transcription.xml%29TEI/text/body/div[@xml:id=%27p"
					+ internalKey.toString()
					+ "%27]");
			connection = (HttpURLConnection) serverAdress.openConnection();			
			connection.setRequestMethod("GET");
			connection.connect();
			if (connection.getResponseCode() == 200) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				stringBuilder = new StringBuilder();
				while((line = reader.readLine()) != null) {
					stringBuilder.append(line + '\n');
				}
				reader.close();
				result = stringBuilder.toString();
			} else {
				LOGGER.error("Request to retrieve ocr text from " + serverAdress + " returned: " + connection.getResponseCode());
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} finally {
			connection.disconnect();
			connection = null;
		}
		return result;
	}
	
	private String getTextContentFromXMLString(final String inputString) {
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
	
	/**
	 * @return the contextType
	 */
	public String getContextType() {
		return CONTEXT_TYPE;
	}

}
