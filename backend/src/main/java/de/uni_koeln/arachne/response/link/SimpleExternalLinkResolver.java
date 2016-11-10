package de.uni_koeln.arachne.response.link;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;

/**
 * Basic link resolver that matches a list of criteria and returns pattern based link URIs.
 * 
 * See method documentation for more information.
 * 
 * @author scuy
 */
public class SimpleExternalLinkResolver implements ExternalLinkResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExternalLinkResolver.class);
	
	private String linkPattern;
	
	private List<String> patternFields;
	
	private String label;
	
	private boolean matchAllCriteria = false;
	
	private boolean exactMatch = true;

	private Map<String,String> criteria;
	
	@Override
	public ExternalLink resolve(Dataset dataset) {
		
		boolean matches = false;
		
		// test if criteria match
		outer: for (String critField : getCriteria().keySet()) {
			String fieldContent = dataset.getField(critField);
			LOGGER.debug("fieldContent: {}", fieldContent);
			String critContent = getCriteria().get(critField);
			if (fieldContent != null) {
				if (critContent.isEmpty()) {
					matches = true;
					if (!isMatchAllCriteria()) break;
				} else {
					String[] critValues = critContent.split(",");
					LOGGER.debug("critValues {}", critValues[0]);
					for (String critValue : critValues) {
						if (isExactMatch()) matches = fieldContent.equals(critValue);
						else matches = fieldContent.contains(critValue);
						if (matches && !isMatchAllCriteria() || !matches && isMatchAllCriteria()) break outer;
					}
				}
			} else {
				matches = false;
				if(isMatchAllCriteria()) break;
			}
		}
		
		// construct link if dataset matches
		if (matches) {
			
			String url = getLinkPattern();
			
			if (getPatternFields() != null && !getPatternFields().isEmpty()) {
				Object[] patternValues = new Object[getPatternFields().size()];
				for (int i = 0; i < getPatternFields().size(); i++) {
					patternValues[i] = dataset.getField(getPatternFields().get(i));
				}
				url = String.format(url, patternValues);
			}
		
			ExternalLink link = new ExternalLink(getLabel(), url);
			return link;
			
		} else {
			return null;
		}
		
	}

	/**
	 * Get the link pattern.
	 * @return the link pattern
	 */
	public String getLinkPattern() {
		return linkPattern;
	}

	/**
	 * Set the link pattern.
	 * @param linkPattern link Pattern in Java format string syntax
	 */
	public void setLinkPattern(String linkPattern) {
		this.linkPattern = linkPattern;
	}

	/**
	 * Get the list of pattern fields.
	 * @return the list of pattern fields
	 */
	public List<String> getPatternFields() {
		return patternFields;
	}

	/**
	 * Set the list of pattern fields.
	 * These are extracted from the dataset and inserted into the link pattern.
	 * @param patternFields the list of fields
	 */
	public void setPatternFields(List<String> patternFields) {
		this.patternFields = patternFields;
	}

	/**
	 * Get the label of the links created by this resolver.
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label of the links created by this resolver.
	 * @param label the label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get matchAllCriteria flag.
	 * @return true if matchAllCriteria flag is set, false otherwise
	 */
	public boolean isMatchAllCriteria() {
		return matchAllCriteria;
	}

	/**
	 * Set matchAllCriteria flag.
	 * This determines if all criteria have to be met in order for a link to be created or just one (default).
	 * @param matchAllCriteria Whether al criteria must match.
	 */
	public void setMatchAllCriteria(boolean matchAllCriteria) {
		this.matchAllCriteria = matchAllCriteria;
	}

	/**
	 * Get exactMatch flag.
	 * @return <code>true</code> if exactMatch flag is set, <code>false</code> otherwise
	 */
	public boolean isExactMatch() {
		return exactMatch;
	}
	
	/**
	 * Set exactMatch flag.
	 * This determines if the values given in the criteria map have to match to the dataset's
	 * field content perfectly (default) or if the field only needs to contain the value as a substring.
	 * @param exactMatch Whether to match the field content exact or as substring.
	 */
	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}

	/**
	 * Get the criteria map.
	 * @return the criteria map
	 */
	public Map<String,String> getCriteria() {
		return criteria;
	}

	/**
	 * Set the criteria map.
	 * The map entries describe properties a dataset has to fulfill in order to be resolve.
	 * Keys correspond to field names, values to field values.
	 * Values can be comma-separated in order to give more than one criterion for the same field name.
	 * @param criteria the criteria map
	 */
	public void setCriteria(Map<String,String> criteria) {
		this.criteria = criteria;
	}	

}
