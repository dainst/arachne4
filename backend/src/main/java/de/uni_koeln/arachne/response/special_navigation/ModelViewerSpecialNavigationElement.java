package de.uni_koeln.arachne.response.special_navigation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * replaced by external link resolver (see external-link-resolvers.xml)
 */
@Deprecated
@Component("modelViewerSpecialNavigationElement")
public class ModelViewerSpecialNavigationElement extends AbstractSpecialNavigationElement {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelViewerSpecialNavigationElement.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentServ;
	
	private transient long modelId;
	
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
		this.modelId = 0;
		
		LOGGER.debug("SearchParams: " + searchParam);
		if (searchParam.matches("[0-9]*")) {
			entityId = entityIdentServ.getId(Long.valueOf(searchParam));		
		}
		
		if (entityId != null && "modell3d".equals(entityId.getTableName())) {
			this.modelId = entityId.getInternalKey();
			returnValue = true;
		}
		return returnValue;
	}

	@Override
	public AbstractSpecialNavigationElement getResult(final String searchParam,
			final String filterValues) {
		final StringBuffer linkBuffer = new StringBuffer(128)
			.append("http://")
			.append(getFullHostName())
			.append(getRequestMapping())
			.append(modelId);
		return new ModelViewerSpecialNavigationElement(linkBuffer.toString());
	}
	
	// TODO move to utility class
	
	/**
	 * Determines the host name as <code>String</code>.
	 * @return The host name of the system or "UnknownHost" in case of failure.
	 */
	private String getFullHostName() {
		String result = "UnknownHost";
		try {
			final InetAddress localHost = InetAddress.getLocalHost();
			result = localHost.getCanonicalHostName();
		} catch (UnknownHostException e) {
			LOGGER.warn("Could not determine local host address.");
		}
		return result;
	}
}