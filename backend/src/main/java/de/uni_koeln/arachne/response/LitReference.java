package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class to hold literature references.
 * @author pfranck
 * @author Reimar Grabowski
 */
@JsonInclude(Include.NON_EMPTY)
public class LitReference {

    /**
     * The reference of the publication, according to arachne.
     */
    private String reference;

    /**
     * The unique identifier in iDAI.bibliography.
     */
    private String zenonId;

    
    /**
     * Constructor setting {@code zenonId} and {@code reference}.
     * 
     * @param zenonId The unique identifier in iDAI.bibliography.
     * @param reference The reference of the publication.
     */
    public LitReference(String zenonId, String reference) {
        if (!StrUtils.isEmptyOrNull(zenonId)) {
            this.zenonId = zenonId;
        }
        if (!StrUtils.isEmptyOrNull(reference)) {
            this.reference = reference;
        }

    }

    /**
	 * Sets the reference of the publication (according to arachne).
	 *
	 * @param reference
	 *            the new reference of the publication (according to arachne).
	 */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
	 * Gets the reference of the publication (according to arachne).
	 *
	 * @return the reference of the publication (according to arachne).
	 */
    public String getReference() {
        return this.reference;
    }

    /**
	 * Sets the iDAI.bibliography unique identifier.
	 *
	 * @param zenonId
	 *            the new iDAI.bibliography unique identifier.
	 */
    public void setZenonId(String zenonId) {
        this.zenonId = zenonId;
    }

    /**
	 * Gets the unique iDAI.bibliography identifier.
	 *
	 * @return the unique iDAI.bibliography identifier.
	 */
    public String getZenonId() {
        return this.zenonId;
    }
}
