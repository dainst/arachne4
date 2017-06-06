package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class to hold date information for entities.
 * @author pfranck
 */
@JsonInclude(Include.NON_EMPTY)
public class LitReference {

    /**
     * the reference of the publication, according to arachne
     */
    private String reference;

    /**
     * all you need is
     */
    private String zenonId;

    /**
     * constructors
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
     * @param reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return String
     */
    public String getReference() {
        return this.reference;
    }

    /**
     * @param zenonId
     */
    public void setZenonId(String zenonId) {
        this.zenonId = zenonId;
    }

    /**
     * @return String
     */
    public String getZenonId() {
        return this.zenonId;
    }
}
