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
	
	public List<SarcophagusImage> getSarcophagusImages() {
		return sarcophagusImages;
	}

	public void setSarcophagusImages(final List<SarcophagusImage> sarcophagusImages) {
		this.sarcophagusImages = sarcophagusImages;
	}
	
	public String getOcrText() {
		return ocrText;
	}

	public void setOcrText(final String ocrText) {
		this.ocrText = ocrText;
	}


}
