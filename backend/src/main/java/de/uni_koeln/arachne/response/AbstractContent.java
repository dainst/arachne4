package de.uni_koeln.arachne.response;


/**
 * Base class for content added to <code>FormattedArachneEntitiy</code>.
 * This class is used both as an entity and as a container for entities.
 * Empty class just for the sake of the composite pattern.
 */
@SuppressWarnings("PMD")
public abstract class AbstractContent { 
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	/** 
	 * Return true so that decisions on lower-level classes are relevant for equality  
	 */
	
	public boolean equals(Object obj) {
		return true;
	}
	
}
