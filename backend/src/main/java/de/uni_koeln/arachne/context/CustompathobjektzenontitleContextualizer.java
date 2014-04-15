package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.Dataset;

/**
 * Adds the 'zenon' context of the 'buch' context to the 'object' dataset as nativ context, so that it is available
 * to add the correct title of the book as literature to the object dataset.
 */
public class CustompathobjektzenontitleContextualizer extends
		AbstractContextualizer {

	protected static final String SQL1 = "SELECT `zenon`.`245_a` FROM `SemanticConnection` `e`, `buch` `b` LEFT JOIN `zenon` ON `zenon`.`001` = `b`.`bibid` WHERE 1 AND `e`.`Source` = ";
	protected static final String SQL2 = " AND `e`.`TypeTarget` = \"buch\" AND `b`.`PS_BuchID` = `e`.`ForeignKeyTarget`"; 
	
	public CustompathobjektzenontitleContextualizer() {
		super();
	}

	@Override
	public String getContextType() {
		return "zenon";
	}

	@Override
	public List<AbstractLink> retrieve(Dataset parent) {
		return null;
	}
		
}
