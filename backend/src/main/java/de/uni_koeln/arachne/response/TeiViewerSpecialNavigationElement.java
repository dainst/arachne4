package de.uni_koeln.arachne.response;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.uni_koeln.arachne.dao.GenericSQLDao;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * 
 * Class contains all necessary information to construct the TEI-Viewer-button 
 * which enables the user to open this browser in an external window
 * @author Sven Ole Clemens
 *
 */
@Component("teiViewerSpecialNavigationElement")
public class TeiViewerSpecialNavigationElement extends AbstractSpecialNavigationElement {

	@Autowired
	private transient EntityIdentificationService entityIdentServ;
	
	@Autowired
	private transient GenericSQLDao genericSQLDao;

	private transient List<String> fieldList;
	
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
		
		if(fieldList != null) {
			fieldList.clear();
		}
		
		EntityId entityId = null;
		
		if(searchParam.matches("[0-9]*")) {
			entityId = entityIdentServ.getId(Long.valueOf(searchParam));		
		}
		
		if(entityId != null && "buch".equals(entityId.getTableName())) {
			fieldList = genericSQLDao.getStringField(entityId.getTableName(), "buch", entityId.getInternalKey(), "Verzeichnis");
			if(fieldList != null && !fieldList.isEmpty()) {
				returnValue = true;
			}
		}
		return returnValue;
	}

	@Override
	public AbstractSpecialNavigationElement getResult(final String searchParam,
			final String filterValues) {
		final StringBuffer linkBuffer = new StringBuffer(getRequestMapping());
		linkBuffer.append("?manifest=");
		linkBuffer.append(fieldList.get(0));
		return new TeiViewerSpecialNavigationElement(linkBuffer.toString());
	}
}
