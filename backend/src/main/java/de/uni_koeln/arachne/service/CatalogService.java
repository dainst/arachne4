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
	
	/**
	 * Creates a PDF representation of a catalog by converting the HTML representation with 'openhtmltopdf'.<br/>
	 * @param catalog The catalog of interest.
	 * @return The PDF file as byte array.
	 */
	/*
	public byte[] getCatalogAsPdf(final Catalog catalog) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
		PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
		
		pdfBuilder.withHtmlContent(getCatalogAsHtml(catalog), "BASEURI");
		pdfBuilder.toStream(baos);
		try {
			pdfBuilder.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return baos.toByteArray();
	}*/
	
	/**
	 * Creates an HTML representation of a catalog. Images are embedded and not links for offline usage.
	 * @param catalog The catalog of interest.
	 * @return The catalog as HTML.
	 */
	public String getCatalogAsHtml(Catalog catalog) {
		final StringBuilder result = new StringBuilder("<!DOCTYPE html><html>");
		result.append("<head><meta charset=\"utf-8\"/><title>" + catalog.getRoot().getLabel() + "</title></head>");
		result.append("<body><header><h2>" + catalog.getRoot().getLabel() + "<br />");
		result.append("<small><sup><i>" + "Autor: " + catalog.getAuthor() + "</i></sup></small></h2></header>");
		result.append("<p>" + catalog.getRoot().getText() + "</p>");
		
		List<CatalogEntry> children = catalog.getRoot().getChildren();
		if (children != null) {
			children.forEach(child -> result.append(addCatalogEntryHtml(child, 0)));
		}
		
		result.append("</body></html>");
		return result.toString();
	}

	/**
	 * Creates a HTML representation of a {@link CatalogEntry} and all its children. 
	 * @param catalogEntry The catalog entry.
	 * @param level The recursion (indentation) level.
	 * @return The entries as HTML.
	 */
	private String addCatalogEntryHtml(final CatalogEntry catalogEntry, final int level) {
		StringBuilder result = new StringBuilder(
				"<p style=\"margin-left: " + level * 20 + "px\"><b>" + catalogEntry.getLabel() + "</b> ");
		
		String idAsString = catalogEntry.getArachneEntityId().toString();
		result.append("<a href=\"https://arachne.dainst.org/entity/" + idAsString  + "\">" + idAsString + "</a></p>");
		
		// image
		EntityId id = entityIdentificationService.getId(catalogEntry.getArachneEntityId());
		TypeWithHTTPStatus<List<Image>> images = imageService.getImagesSubList(id, 0, 1);
		if (images.getStatus().equals(HttpStatus.OK)) {
			byte[] img = iipService.getImage(images.getValue().get(0).getImageId(), 300, 300).getValue();
			result.append("<img style=\"margin-left: " + level * 20 + "px\" "
					+ "src='data:image/jpeg;base64," + DatatypeConverter.printBase64Binary(img) + "' />");
		}
		
		// text
		String entryText = catalogEntry.getText();
		if (!StrUtils.isEmptyOrNull(entryText)) {
			result.append("<p style=\"margin-left: " + level * 20 + "px\">" + entryText + "</p>");
		}
		
		result.append("<br />");
		
		// children
		List<CatalogEntry> children = catalogEntry.getChildren();
		if (children != null) {
			children.forEach(child -> result.append(addCatalogEntryHtml(child, level + 1)));
		}
		return result.toString();
	}
}
