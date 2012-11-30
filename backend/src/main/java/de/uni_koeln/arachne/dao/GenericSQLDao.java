package de.uni_koeln.arachne.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.EntityIdMapper;
import de.uni_koeln.arachne.mapping.GenericFieldMapperString;
import de.uni_koeln.arachne.service.SQLResponseObject;
import de.uni_koeln.arachne.sqlutil.ConnectedEntityIdsSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.ConnectedEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsEntityIdJoinedSQLQueryBuilder;

/**
 * Class to retrieve data via SQL.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	//TODO Exclude
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericSQLDao.class);
	
	public List<String> getStringField(final String tableName, final String field1, final Long field1Id
			, final String field2, final boolean disableAuthorization) {
		final GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1
				, field1Id, field2, disableAuthorization);
		@SuppressWarnings("unchecked")
		final List<String> queryResult = (List<String>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperString());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<String> getStringField(final String tableName, final String field1, final Long field1Id
			, final String field2) {
		return getStringField(tableName, field1, field1Id, field2, false);		
	}
	
	public List<Map<String, String>> getConnectedEntities(final String contextType, final Long entityId) {
		final ConnectedEntitiesSQLQueryBuilder queryBuilder = new ConnectedEntitiesSQLQueryBuilder(contextType, entityId);
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Long> getConnectedEntityIds(final Long entityId) {
		final ConnectedEntityIdsSQLQueryBuilder queryBuilder = new ConnectedEntityIdsSQLQueryBuilder(entityId);
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)this.executeSelectQuery(queryBuilder.getSQL()
				, new EntityIdMapper());
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	public List<Long> getPathConnectedEntityIds(final Long entityId, List<String> typeStepRestriction) {
		
		//First Things doesnt belong here
		//First Things
		//TODO Create SQL builder
		StringBuilder BSPQuery = new StringBuilder( "SELECT e"+ (typeStepRestriction.size()-1) +".Target FROM SemanticConnection e0, ");
		
		
		//Declare the Variables
		for(int i =0;  i< typeStepRestriction.size()-2;i++ ){ 
			BSPQuery.append( " SemanticConnection e"+(i+1)+" ,");
		}
		if(typeStepRestriction.size() >1)
			BSPQuery.append( " SemanticConnection e"+(typeStepRestriction.size()-1)+" ");
		//BSPQuery.append( " SemanticConnection e"+typeStepRestriction.size()+" WHERE");
		
		BSPQuery.append( " WHERE 1 AND ");
		
		//Chain Logic e1.Target = e2.Source ... e2.Target = e3.source ETC
		
		
		for(int i =0;  i< typeStepRestriction.size()-1;i++ ){ 
			BSPQuery.append( " e"+i+".Target = e"+(i+1)+".Source AND ");
		}
		
		
		//This Sets the Source ID
		BSPQuery.append( " e0.Source ="+entityId+ " AND");
		
		for(int i =0;  i< typeStepRestriction.size();i++ ){
			String temp = typeStepRestriction.get(i);
			if(!"*".equals(temp)){
				BSPQuery.append(" e"+i+".TypeTarget = \""+typeStepRestriction.get(i)+"\" AND ");
			}
		
		}
		//BSPQuery.append(" e"+(typeStepRestriction.size()-1)+".TypeTarget = \""+typeStepRestriction.get(typeStepRestriction.size()-1)+"\"");
								
		BSPQuery.append("1 GROUP BY e"+(typeStepRestriction.size()-1)+".Target");
		LOGGER.debug(BSPQuery.toString());
		
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)this.executeSelectQuery(BSPQuery.toString()
				, new EntityIdMapper());
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	
	
	public List<? extends SQLResponseObject> getStringFieldsEntityIdJoinedWithCustomRowMapper(
			final String tableName, final String field1, final Long field1Id, final List<String> fields
			, final RowMapper<? extends SQLResponseObject> rowMapper) {
		final GenericFieldsEntityIdJoinedSQLQueryBuilder queryBuilder = new GenericFieldsEntityIdJoinedSQLQueryBuilder(
				tableName, field1, field1Id, fields);
		@SuppressWarnings("unchecked")
		final List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)this.executeSelectQuery(
				queryBuilder.getSQL(), rowMapper);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
}
