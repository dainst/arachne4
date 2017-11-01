package de.uni_koeln.arachne.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

/**
 * Class to output catalogs in different formats.<br/>
 * Do not use this class in its current state as it cannot handle large catalogs (like emagines), yet.
 * @author Reimar Grabowski
 *
 */
@Service
public class CatalogService {
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient ImageService imageService;
	
	@Autowired
	private transient IIPService iipService; 

	



}
