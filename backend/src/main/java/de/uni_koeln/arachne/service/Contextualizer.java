package de.uni_koeln.arachne.service;

import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.Link;

public interface Contextualizer {
	abstract String getContextType();
	abstract List<Link> retrive(ArachneDataset Source, Integer offset, Integer limit);

}
