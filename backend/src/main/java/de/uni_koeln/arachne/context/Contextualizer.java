package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;

/**
 * This Interface Describes the Minimum Functions a Context retrivers must Implement
 * 
 */
public interface Contextualizer {
	/**
	 * Returns the Type of Context this Contextualizer serves
	 * @return String that Describes the Context Tool Serves example "ort"
	 */
	public String getContextType();
	
	/**
	 * 
	 * @param Source The Dataset form which the Context is Created
	 * @param Offset The Start of the Stuff
	 * @param limit The Number of Contextes to get
	 * @return The Links that Represent the Context
	 */
	public List<Link> retrive(ArachneDataset Source, Integer offset, Integer limit);
}
