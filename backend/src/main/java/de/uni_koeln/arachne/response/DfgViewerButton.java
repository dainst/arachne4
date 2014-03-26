package de.uni_koeln.arachne.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

@Component("DfgViewerButton")
public class DfgViewerButton extends AbstractSpecialNavigationElement {

	private static final Logger LOGGER = LoggerFactory.getLogger(DfgViewerButton.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentServ;
	
	private transient String link = null;
	
	@Value("#{config.dfgViewerLink}")
	private transient String dfgViewerLink;
	
	public DfgViewerButton() {
		super();
	}
	
	protected DfgViewerButton(final String link) {
		super(link);
	}
	
	@Override
	public SpecialNavigationElementTypeEnum getType() {
		return SpecialNavigationElementTypeEnum.BUTTON;
	}

	@Override
	public SpecialNavigationElementTargetEnum getTarget() {
		return SpecialNavigationElementTargetEnum.EXTERN;
	}

	@Override
	public String getTitle() {
		return "DFG-Viewer";
	}

	@Override
	public String getRequestMapping() {
		return dfgViewerLink;
	}

	@Override
	public String getName() {
		return "DFG-Viewer";
	}

	@Override
	public boolean matches(final String searchParam, final String filterValues) {
		boolean returnValue = false;
		
		EntityId entityId = null;
		
		if(searchParam.matches("[0-9]*")) {
			entityId = entityIdentServ.getId(Long.valueOf(searchParam));		
		}
		
		if (entityId != null) {
			if ("buch".equals(entityId.getTableName())) {
				final StringBuffer linkBuffer = new StringBuffer(getRequestMapping());
				linkBuffer.append(entityId.getInternalKey());
				linkBuffer.append("%26metadataPrefix%3Dmets");
				link = linkBuffer.toString();
			}
			
			if (!StrUtils.isEmptyOrNull(link)) {
				returnValue = true;
			}
		}
		return returnValue;
	}

	@Override
	public AbstractSpecialNavigationElement getResult(final String searchParam,
			final String filterValues) {
		
		return new DfgViewerButton(link);
	}

}
