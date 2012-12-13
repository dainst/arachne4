package de.uni_koeln.arachne.response;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to wrap miscellaneous items in the <code>Dataset</code> field AdditionalContent.
 * The sole purpose of this class is to allow easy XML serialization by JAXB.
 */
@XmlRootElement
public class AdditionalContent {
	
	private Set<SarcophagusImage> sarcophagusImages;

	public Set<SarcophagusImage> getSarcophagusImages() {
		return sarcophagusImages;
	}

	public void setSarcophagusImages(final Set<SarcophagusImage> sarcophagusImages) {
		this.sarcophagusImages = sarcophagusImages;
	}

}
