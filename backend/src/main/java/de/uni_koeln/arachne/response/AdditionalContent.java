package de.uni_koeln.arachne.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to wrap miscellaneous items in the <code>Dataset</code> field AdditionalContent.
 * The sole purpose of this class is to allow easy XML serialization by JAXB.
 */
@XmlRootElement
public class AdditionalContent {
	
	private List<SarcophagusImage> sarcophagusImages;
	
	private String ocrText;
	
	/**
	 * Getter for sarcophagus images.
	 * @return The list of sarcophagus images.
	 */
	public List<SarcophagusImage> getSarcophagusImages() {
		return sarcophagusImages;
	}

	/**
	 * Setter for sarcophagus images.
	 * @param sarcophagusImages A list of sarcophagus images.
	 */
	public void setSarcophagusImages(final List<SarcophagusImage> sarcophagusImages) {
		this.sarcophagusImages = sarcophagusImages;
	}
	
	/**
	 * Getter for OCR text.
	 * @return The OCR text.
	 */
	public String getOcrText() {
		return ocrText;
	}

	/**
	 * Setter for OCR text.
	 * @param ocrText An OCR text.
	 */
	public void setOcrText(final String ocrText) {
		this.ocrText = ocrText;
	}


}
