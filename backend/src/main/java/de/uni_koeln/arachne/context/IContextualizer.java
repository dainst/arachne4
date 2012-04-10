package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.Dataset;

/**
 * This Interface describes the minimum functions a context retriever must implement.
 */
public interface IContextualizer {
	/**
	 * Returns the type of context this Contextualizer serves.
	 * @return String that Describes the Context Tool Serves example "ort".
	 */
	public String getContextType();
	
	/**
	 * Retrieval function to get the contexts. They may be fetched from the database or an external data source.
	 * @param parent The dataset for which the context is created.
	 * @param offset Sets which context is the first to get.
	 * @param limit The number of contexts to get.
	 * @return The links that represent the contexts.
	 */
	public List<AbstractLink> retrieve(Dataset parent, Integer offset, Integer limit);
}
