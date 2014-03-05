package de.uni_koeln.arachne.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.util.EntityId;

@Component("modelViewerSpecialNavigationElement")
public class ModelViewerSpecialNavigationElement extends AbstractSpecialNavigationElement {

	//private static final Logger LOGGER = LoggerFactory.getLogger(ModelViewerSpecialNavigationElement.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentServ;
	
	private transient long entityId;
	
	@Value("#{config.modelViewerLink}")
	private transient String modelViewerLink;
	
	public ModelViewerSpecialNavigationElement() {
		super();
	}
	
	protected ModelViewerSpecialNavigationElement(final String link) {
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
		return "3D Model Viewer";
	}

	@Override
	public String getRequestMapping() {
		return modelViewerLink;
	}

	@Override
	public String getName() {
		return "3D Model Viewer";
	}

	@Override
	public boolean matches(final String searchParam, final String filterValues) {
		boolean returnValue = false;
		
		EntityId entityId = null;
		this.entityId = 0;
		
		if (searchParam.matches("[0-9]*")) {
			entityId = entityIdentServ.getId(Long.valueOf(searchParam));		
		}
		
		if (entityId != null && "modell3d".equals(entityId.getTableName())) {
			this.entityId = entityId.getArachneEntityID();
			returnValue = true;
		}
		return returnValue;
	}

	@Override
	public AbstractSpecialNavigationElement getResult(final String searchParam,
			final String filterValues) {
		final StringBuffer linkBuffer = new StringBuffer(getRequestMapping());
		linkBuffer.append("/");
		linkBuffer.append(entityId);
		return new ModelViewerSpecialNavigationElement(linkBuffer.toString());
	}
}