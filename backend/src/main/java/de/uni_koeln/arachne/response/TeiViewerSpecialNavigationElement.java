package de.uni_koeln.arachne.response;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class TeiViewerSpecialNavigationElement extends
		AbstractSpecialNavigationElement {

	private transient EntityIdentificationService entityIdentServ;
	
	private transient GenericSQLDao genericSQLDao;

	private transient List<String> fieldList;
	
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
		return "http://arachne.uni-koeln.de/Tei-Viewer/cgi-bin/teiviewer.php";
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
		
		if("buch".equals(entityId.getTableName())) {
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
		//linkBuffer.append(searchParam.replace(':', '='));
		linkBuffer.append("?manifest=");
		linkBuffer.append(fieldList.get(0));
		return new TeiViewerSpecialNavigationElement(linkBuffer.toString());
	}
	
	@Autowired
	@Qualifier("arachneEntityIdentificationService")
	public void setEntityIdentificationService(final EntityIdentificationService entityIdentServ) {
		this.entityIdentServ = entityIdentServ;
	}
	
	@Autowired
	@Qualifier("GenericSQLDao")
	public void setGenericSQLDao (final GenericSQLDao genericSQLDao) {
		this.genericSQLDao = genericSQLDao;
	}

}
