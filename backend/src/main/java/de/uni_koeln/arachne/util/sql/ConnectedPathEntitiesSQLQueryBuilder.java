package de.uni_koeln.arachne.util.sql;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.uni_koeln.arachne.context.ContextPath;
import de.uni_koeln.arachne.service.IUserRightsService;

/**
 * This Class is Builds Context Paths. This Means a Link over one or More Contexts. 
 * There for it uses the SemanticConnection Table which Contains All Connections.
 */
@Configurable(preConstruction=true)
public class ConnectedPathEntitiesSQLQueryBuilder extends AbstractSQLBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedPathEntitiesSQLQueryBuilder.class);
	
	@Autowired
	transient protected IUserRightsService userRightsService;
	
	transient protected ContextPath contextPath;
	//Entity ID that is the Startingpoint of the PATH
	transient protected Long entityStart; 
	
	//TODO Implement Version with Key Table Solution
	transient protected String table;
	
	transient protected boolean getFullDataset = false;
	
	transient protected Long foreignKey;
	
	public ConnectedPathEntitiesSQLQueryBuilder(final ContextPath contextPath, final Long entityStart) {
		this.contextPath = contextPath;
		this.entityStart = entityStart;
	}
	/**
	 * This is the Button to retrive the full connected Dataset (Yes) or just the Entity ID (NO)
	 * @param getFullDataset Yes or No. Default: NO!
	 */
	public void retriveFullDataset(final boolean getFullDataset) {
		this.getFullDataset = getFullDataset;
	}
	
	@Override
	protected void buildSQL() {
		final List<String> typeStepRestrictions = this.contextPath.getTypeStepRestrictions();
		if (typeStepRestrictions.size() < 5) {
			final StringBuilder result = new StringBuilder(128);

			if( this.getFullDataset){
				result.append( "SELECT e"+ (typeStepRestrictions.size()-1) +".Target as EntityID, e"+ (typeStepRestrictions.size()-1) +".ForeignKeyTarget as ForeignKeyTarget, `"+contextPath.getTargetType()  +"`.* FROM SemanticConnection e0, ");
			}else{
				result.append( "SELECT e"+ (typeStepRestrictions.size()-1) +".Target FROM SemanticConnection e0, ");
			}

			//Declare the Variables
			for (int i = 0;  i < typeStepRestrictions.size()-2; i++) { 
				result.append( " SemanticConnection e"+(i+1)+" ,");
			}
			if (typeStepRestrictions.size() > 1) {
				result.append( " SemanticConnection e"+(typeStepRestrictions.size()-1)+" ");
			}
			if (this.getFullDataset){
				result.append(" LEFT JOIN `");
				result.append(contextPath.getTargetType());
				result.append("` ON ");
				result.append(SQLToolbox.getQualifiedFieldname(contextPath.getTargetType(), SQLToolbox.generatePrimaryKeyName(contextPath.getTargetType())));
				result.append(" = `e"+(typeStepRestrictions.size()-1)+"`.`ForeignKeyTarget`");
			}
			result.append( " WHERE 1 AND ");

			//Chain Logic e1.Target = e2.Source ... e2.Target = e3.source ETC
			result.append(buildLenghtChain());

			//This Sets the Source ID
			result.append(" e0.Source =" + entityStart + " AND");

			for (int i = 0; i < typeStepRestrictions.size(); i++) {
				final String temp = typeStepRestrictions.get(i);
				if (!"ALL".equals(temp)){
					result.append(" e"+i+".TypeTarget = \""+typeStepRestrictions.get(i)+"\" ");
					if(i< typeStepRestrictions.size()-1){
						result.append(" AND ");
					}
				}

			}

			//BSPQuery.append(" e"+(typeStepRestriction.size()-1)+".TypeTarget = \""+typeStepRestriction.get(typeStepRestriction.size()-1)+"\"");
			if (this.getFullDataset){
				result.append(userRightsService.getSQL(this.contextPath.getTargetType()));
			}
			result.append(" GROUP BY e"+(typeStepRestrictions.size()-1)+".Target");
			sql = result.toString();
		} else {
			LOGGER.error("Too many step when trying to get connected entities for [" + entityStart + "].");
		}
	}	
	
	private StringBuilder buildLenghtChain(){
		final List<String> typeStepRestrictions = this.contextPath.getTypeStepRestrictions();
		final StringBuilder out = new StringBuilder(32); 
		for(int i =0;  i< typeStepRestrictions.size()-1;i++ ){ 
			out.append( " e"+i+".Target = e"+(i+1)+".Source AND ");
		}
		return out;
	}

}
