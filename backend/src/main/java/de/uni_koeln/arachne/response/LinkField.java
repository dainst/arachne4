package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *  Class that holds and ouputs a HTML formatted limk.
 */
public class LinkField extends Field {
	private transient String labelKey;
	
	/**
	 * Convenience constructor to initialize <code>labelKey</code> at construction time.
	 * @param labelKey The String to display as link text.
	 */
	public LinkField(final String labelKey) {
		this.labelKey = labelKey;
	}
	
	@JsonIgnore
	public String getLabelKey() {
		return this.labelKey;
	}
	
	public void setLabelKey(final String labelKey) {
		this.labelKey = labelKey;
	}
	
	/**
	 * Returns a String containing the values of this class as HTML link.
	 * @return A HTML tag that is created from <code>value</code> and <code>labelKey</code>.
	 */
	public void convertValueToLink() {
		this.value = "<a href=\"" + this.value + "\" target=\"_blank\">" + this.labelKey + "</a>";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((labelKey == null) ? 0 : labelKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LinkField other = (LinkField) obj;
		if (labelKey == null) {
			if (other.labelKey != null) {
				return false;
			}
		} else {
			if (!labelKey.equals(other.labelKey)) {
				return false;
			}
		}
		return true;
	}


}

