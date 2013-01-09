package de.uni_koeln.arachne.sqlutil;

import java.util.List;



import de.uni_koeln.arachne.context.ContextPath;
/**
 * This Class is Builds Context Paths. This Means a Link over one or More Contexts. 
 * There for it uses the SemanticConnection Table which Contains All Connections.
 */
public class ConnectedPathEntitiesSQLQueryBuilder extends AbstractSQLBuilder {
	
	
	transient protected SQLRightsConditionBuilder rightsConditionBuilder;
	
	transient protected ContextPath contextPath;
	//Entity ID that is the Startingpoint of the PATH
	transient protected Long entityStart; 
	
	//TODO Implement Version with Key Table Solution
	transient protected String table;
	
	transient protected boolean getFullDataset= false;
	
	transient protected Long foreignKey;
	
	public  ConnectedPathEntitiesSQLQueryBuilder(final ContextPath contextPath, final Long entityStart) {
		this.contextPath = contextPath;
		this.entityStart = entityStart;
		rightsConditionBuilder= new SQLRightsConditionBuilder(this.contextPath.getTargetType());
	}
	/**
	 * This is the Button to retrive the full connected Dataset (Yes) or just the Entity ID (NO)
	 * @param getFullDataset Yes or No. Default: NO!
	 */
	public void retriveFullDataset(final boolean getFullDataset) {
		this.getFullDataset = getFullDataset;
	}
	
	@Override
	protected String buildSQL() {
		//First Things doesnt belong here
		//First Things
		
		final List<String> typeStepRestrictions = this.contextPath.getTypeStepRestrictions();
		
		
		if(this.getFullDataset){
			sql.append( "SELECT e"+ (typeStepRestrictions.size()-1) +".Target as EntityID, e"+ (typeStepRestrictions.size()-1) +".ForeignKeyTarget as ForeignKeyTarget, `"+contextPath.getTargetType()  +"`.* FROM SemanticConnection e0, ");
		}else{
			sql.append( "SELECT e"+ (typeStepRestrictions.size()-1) +".Target FROM SemanticConnection e0, ");
		}
		
		//Declare the Variables
		for(int i =0;  i< typeStepRestrictions.size()-2;i++ ){ 
			sql.append( " SemanticConnection e"+(i+1)+" ,");
		}
		if(typeStepRestrictions.size() >1){
			sql.append( " SemanticConnection e"+(typeStepRestrictions.size()-1)+" ");
		}
		if(this.getFullDataset){
			sql.append(" LEFT JOIN `");
			sql.append(contextPath.getTargetType());
			sql.append("` ON ");
			sql.append(SQLToolbox.getQualifiedFieldname(contextPath.getTargetType(), SQLToolbox.generatePrimaryKeyName(contextPath.getTargetType())));
			sql.append(" = `e"+(typeStepRestrictions.size()-1)+"`.`ForeignKeyTarget`");
		}
		sql.append( " WHERE 1 AND ");
		
		//Chain Logic e1.Target = e2.Source ... e2.Target = e3.source ETC
		
		
		sql.append(buildLenghtChain());
		
		
		
		//This Sets the Source ID
		sql.append( " e0.Source ="+entityStart+ " AND");
		
		for(int i =0;  i< typeStepRestrictions.size();i++ ){
			final String temp = typeStepRestrictions.get(i);
			if(!"ALL".equals(temp)){
				sql.append(" e"+i+".TypeTarget = \""+typeStepRestrictions.get(i)+"\" ");
				if(i< typeStepRestrictions.size()-1){
					sql.append(" AND ");
				}
			}
		
		}
		
		//BSPQuery.append(" e"+(typeStepRestriction.size()-1)+".TypeTarget = \""+typeStepRestriction.get(typeStepRestriction.size()-1)+"\"");
		if(this.getFullDataset){
			sql.append(rightsConditionBuilder.getUserRightsSQLSnipplett());
		}
		sql.append(" GROUP BY e"+(typeStepRestrictions.size()-1)+".Target");

		
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
