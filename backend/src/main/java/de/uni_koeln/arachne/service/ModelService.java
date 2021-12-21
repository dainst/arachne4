package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Model;
import de.uni_koeln.arachne.util.EntityId;

/**
 * This service class provides the means to retrieve model meta data from the
 * database.
 * 
 * @author Sebastian Cuy
 */
@Service("ModelService")
public class ModelService {
	
	@Autowired
	private GenericSQLDao genericSQLDao; 
	
	private transient final List<String> includeList;
	
	/**
	 * Constructor setting the list of types that do not have any connected models.
	 * @param modelIncludeList The list of types without images.
	 */
	@Autowired
	public ModelService(final @Value("#{'${modelIncludeList}'.split(',')}") List<String> modelIncludeList) {
		includeList = modelIncludeList;
	}
    
    /**
	 * This method retrieves the model ids for a given dataset from the database and adds them to the datasets list
	 * of 3d models.
	 * @param dataset The dataset to add models to.
	 */
	public void addModels(final Dataset dataset) {
		final EntityId arachneId = dataset.getArachneId();
		if (!includeList.contains(arachneId.getTableName())) {
			return;
		} else {
			if ("modell3d".equals(arachneId.getTableName())) {
				final Model model = new Model();
				model.setModelId(arachneId.getArachneEntityID());
				model.setTitle(dataset.getField("modell3d.Titel"));
				model.setFileName(dataset.getField("modell3d.Dateiname"));
				model.setInternalId(dataset.getArachneId().getInternalKey());
				final List<Model> modelList = new ArrayList<>();
				modelList.add(model);
				dataset.setModels(modelList);
			} else {
				final List<Model> modelList = (List<Model>) genericSQLDao.getModelList(arachneId.getTableName()
						, arachneId.getInternalKey());
				dataset.setModels(modelList);
			}
		}
	}

}
