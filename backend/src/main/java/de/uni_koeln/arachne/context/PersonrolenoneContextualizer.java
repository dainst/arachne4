package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;

public class PersonrolenoneContextualizer extends AbstractSemanticConnectionFilterContextualizer {

	@Override
	public String getContextType() {
		return "personrolenone";
	}

	@Override
	public String getTargetName() {
		return "person";
	}

	@Override
	public boolean filter(Dataset parent, Dataset other) {
		final String role = other.getField("person.Rolle");
		return StrUtils.isEmptyOrNull(role);
	}
}