package de.uni_koeln.arachne.context;

import java.util.regex.Pattern;

import de.uni_koeln.arachne.response.Dataset;

public class PersonroleaddresseeContextualizer extends AbstractSemanticConnectionFilterContextualizer {

	private static final Pattern PATTERN = Pattern.compile("^adressat$", Pattern.CASE_INSENSITIVE);

	@Override
	public String getContextType() {
		return "personroleaddressee";
	}

	@Override
	public String getTargetName() {
		return "person";
	}

	@Override
	public boolean filter(Dataset parent, Dataset other) {
		final String role = other.getField("person.Rolle");
		return role != null && PATTERN.matcher(role.trim()).matches();
	}
}