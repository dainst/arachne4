package de.uni_koeln.arachne.sqlutil;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.context.ContextPath;

public class ConnectedPathEntitiesSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedEntitiesSQLQueryBuilder.class);
	
	protected transient SQLRightsConditionBuilder rightsConditionBuilder;
	
	protected transient ContextPath contextPath;
	//Entity ID that is the Startingpoint of the PATH
	protected transient Long entityStart; 
	
	//TODO Implement Version with Key Table Solution
	protected String table;
	
	protected Long foreignKey;
	
	public ConnectedPathEntitiesSQLQueryBuilder(final ContextPath contextPath, final Long entityStart) {
		this.contextPath = contextPath;
		this.entityStart = entityStart;
		
	}
	
	@Override
	protected String buildSQL() {
		//First Things doesnt belong here
		//First Things
		
		final List<String> typeStepRestrictions = this.contextPath.getTypeStepRestrictions();
		
		sql.append( "SELECT e"+ (typeStepRestrictions.size()-1) +".Target FROM SemanticConnection e0, ");
		
		
		//Declare the Variables
		for (int i = 0;  i < typeStepRestrictions.size() - 2; i++) { 
			sql.append( " SemanticConnection e"+(i+1)+" ,");
		}
		if (typeStepRestrictions.size() > 1) {
			sql.append( " SemanticConnection e"+(typeStepRestrictions.size()-1)+" ");
		}
		//BSPQuery.append( " SemanticConnection e"+typeStepRestriction.size()+" WHERE");
		
		sql.append( " WHERE 1 AND ");
		
		//Chain Logic e1.Target = e2.Source ... e2.Target = e3.source ETC
		
		
		sql.append(buildLenghtChain());
		
		
		
		//This Sets the Source ID
		sql.append( " e0.Source ="+entityStart+ " AND");
		
		for (int i =0;  i < typeStepRestrictions.size(); i++) {
			final String temp = typeStepRestrictions.get(i);
			if (!"ALL".equals(temp)) {
				sql.append(" e"+i+".TypeTarget = \""+typeStepRestrictions.get(i)+"\" AND ");
			}
		
		}
		//BSPQuery.append(" e"+(typeStepRestriction.size()-1)+".TypeTarget = \""+typeStepRestriction.get(typeStepRestriction.size()-1)+"\"");
								
		sql.append("1 GROUP BY e"+(typeStepRestrictions.size()-1)+".Target");

		LOGGER.debug(sql.toString());
		return sql.toString();
	}
	
	
	private StringBuilder buildLenghtChain(){
		final List<String> typeStepRestrictions = this.contextPath.getTypeStepRestrictions();
		final StringBuilder out = new StringBuilder(); 
		for(int i =0;  i< typeStepRestrictions.size()-1;i++ ){ 
			out.append( " e"+i+".Target = e"+(i+1)+".Source AND ");
		}
		return out;
		
	}
	
	
	

}
