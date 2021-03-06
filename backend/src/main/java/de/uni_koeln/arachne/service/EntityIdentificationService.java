package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.hibernate.ArachneEntity;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Service class to retrieve information from the <code>arachneentityidentification</code> table.
 * 
 * @author Reimar Grabowski
 *
 */
@Service("arachneEntityIdentificationService")
public class EntityIdentificationService {
	
	@Autowired
	private transient ArachneEntityDao arachneEntityDao; 
	
	/**
	 * Gets all identifiers of a dataset by Arachne entity ID. This is the external reference ID for the dataset in Arachne.
	 * @param arachneEntityId The unique Arachne identifier.
	 * @return an <code>ArachneId</code> object that contains all the identification information.
	 */
	public EntityId getId(final Long arachneEntityId) {
		return getByEntityId(arachneEntityId);
	}
	
	/**
	 * Gets all identifiers of a dataset by table name and internal key. This is the external reference ID for the dataset in Arachne.
	 * @param tableName The name of the SQL table.
	 * @param internalKey The internalkey of the entity. 
	 * @return an <code>ArachneId</code> object that contains all the identification information.
	 */
	public EntityId getId(final String tableName, final Long internalKey) {
		return getByTablenameAndInternalKey(tableName, internalKey);
	}
	
	/**
	 * Gets a range of <code>ArachneEntityIds</code>.
	 * @param startId The first id in the range.
	 * @param limit The number of entities in the returned list. 
	 * @return a list of <code>ArachneId</code>s.
	 */
	public List<ArachneEntity> getByLimitedEntityIdRange(final long startId, final int limit) {
		return arachneEntityDao.getByLimitedEntityIdRange(startId, limit);
	}

	/**
	 * Get the currently active Entity-ID for the given image file name.
	 * @param imageFilename The image file name.
	 * @return an <code>ArachneId</code> object that contains all the identification information.
	 */
	public EntityId getNotDeletedByImageFilename(final String imageFilename) {
		ArachneEntity entity = arachneEntityDao.getNotDeletedByImageFilename(imageFilename);
		if (entity != null) {
			return new EntityId(entity);
		}
		return null;
	}
	
	/**
	 * Gets all identifiers of a dataset by Arachne entity ID. This is the external reference ID for the dataset in Arachne.
	 * For convenience the public method <code>getId</code> is overloaded.
	 * @param entityId The Arachne entity ID.
	 * @return an <code>ArachneId</code> object that contains all the identification information.
	 */
	private EntityId getByEntityId(final Long entityId) {
		ArachneEntity entity = arachneEntityDao.getByEntityID(entityId);
		if (entity != null) {
			return new EntityId(entity);
		}
		return null;
	}
	
	/**
	 * Gets all identifiers of a Dataset by tablename and primary key.
	 * For convenience the public method <code>getId</code> is overloaded.
	 * @param tableName The internal table name in the Arachne database.
	 * @param internalKey internal table key of the dataset
	 * @return an <code>ArachneId</code> object that contains all the identification information.
	 */
	private EntityId getByTablenameAndInternalKey(final String tableName, final Long internalKey){
		return new EntityId(arachneEntityDao.getByTablenameAndInternalKey(tableName, internalKey));
	}
}
