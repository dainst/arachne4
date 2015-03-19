package de.uni_koeln.arachne.response.special_navigation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * 
 * Class contains all necessary information to construct the TEI-Viewer-button 
 * which enables the user to open this browser in an external window
 * 
 * replaced by external link resolver (see external-link-resolvers.xml)
 * 
 * @author Sven Ole Clemens
 *
 */
@Deprecated
@Component("teiViewerSpecialNavigationElement")
public class TeiViewerSpecialNavigationElement extends AbstractSpecialNavigationElement {

	private static final Logger LOGGER = LoggerFactory.getLogger(TeiViewerSpecialNavigationElement.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentServ;
	
	@Autowired
	private transient GenericSQLDao genericSQLDao;

	private transient String link = null;
	
	@Value("#{config.teiViewerLink}")
	private transient String teiViewerLink;
	
	public TeiViewerSpecialNavigationElement() {
		super();
	}
	
	protected TeiViewerSpecialNavigationElement(final String link) {
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
		return "TEI-Viewer";
	}

	@Override
	public String getRequestMapping() {
		return teiViewerLink;
	}

	@Override
	public String getName() {
		return "TEI-Viewer";
	}

	@Override
	public boolean matches(final String searchParam, final String filterValues) {
		boolean returnValue = false;
		
		EntityId entityId = null;
		
		LOGGER.debug("SearchParam: " + searchParam);
		if(searchParam.matches("[0-9]*")) {
			entityId = entityIdentServ.getId(Long.valueOf(searchParam));		
		}
		
		if (entityId != null) {
			String fieldList = null;
			if ("buch".equals(entityId.getTableName())) {
				fieldList = genericSQLDao.getStringField(entityId.getTableName(), "buch", entityId.getInternalKey(), "Verzeichnis");
				final StringBuffer linkBuffer = new StringBuffer(getRequestMapping());
				linkBuffer.append("?manifest=");
				linkBuffer.append(fieldList);
				link = linkBuffer.toString();
			} else { 
				if ("buchseite".equals(entityId.getTableName())) {
					String scanName = genericSQLDao.getStringField("marbilder", "Buchseite", entityId.getInternalKey(), "DateinameMarbilder");
					if (!StrUtils.isEmptyOrNull(scanName)) {
						final StringBuffer linkBuffer = new StringBuffer(getRequestMapping());
						linkBuffer.append("?scan=");
						linkBuffer.append(scanName.substring(0, scanName.indexOf('.')));
						link = linkBuffer.toString();	
					}
				} else {
					return false;
				}
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
		
		return new TeiViewerSpecialNavigationElement(link);
	}
}
