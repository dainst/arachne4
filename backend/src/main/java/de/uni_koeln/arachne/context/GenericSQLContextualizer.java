package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;

/**
 * This is the default <code>Contextualizer</code> the <code>ContextService</code> uses if 
 * no specialized one is specified.   
 */
public class GenericSQLContextualizer implements IContextualizer {

	@Override
	public String getContextType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> retrieve(ArachneDataset parent, Integer offset,
			Integer limit) {
		// TODO Auto-generated method stub
		return null;
	}

}
