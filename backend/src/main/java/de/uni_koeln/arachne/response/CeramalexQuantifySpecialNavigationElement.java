package de.uni_koeln.arachne.response;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * 
 * Class contains all necessary information to construct the quantify-button 
 * which enables the use of the quantify-functionality of the CeramalexController
 * @author Patrick Gunia
 *
 */

public class CeramalexQuantifySpecialNavigationElement extends AbstractSpecialNavigationElement {
	
	public CeramalexQuantifySpecialNavigationElement() {
		super();
	}
	
	protected CeramalexQuantifySpecialNavigationElement(String link) {
		super(link);
	}

	@Override
	public boolean matches(final String searchParam, final String filterValues) {
		if(filterValues == null || filterValues.isEmpty() || !filterValues.contains("facet_kategorie:\"mainabstract\"")) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public AbstractSpecialNavigationElement getResult(final String searchParam,
			final String filterValues) {
		final StringBuffer linkBuffer = new StringBuffer(getRequestMapping());
		linkBuffer.append("?q=");
		linkBuffer.append(searchParam);
		linkBuffer.append("&fq=");
		linkBuffer.append(StrUtils.urlEncodeQuotationMarks(filterValues));
		return new CeramalexQuantifySpecialNavigationElement(linkBuffer.toString());
	}

	@Override
	public SpecialNavigationElementTypeEnum getType() {
		return SpecialNavigationElementTypeEnum.BUTTON;
	}

	@Override
	public SpecialNavigationElementTargetEnum getTarget() {
		return SpecialNavigationElementTargetEnum.INTERN;
	}

	@Override
	public String getTitle() {
		return "Quantify";
	}

	@Override
	public String getRequestMapping() {
		return "project/ceramalex/quantify";
	}

	@Override
	public String getName() {
		return "ceramalex";
	}	
}
