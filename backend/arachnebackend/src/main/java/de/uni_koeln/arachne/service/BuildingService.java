package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.BuildingDao;
import de.uni_koeln.arachne.mapping.Building;

@Service("buildingService")
public class BuildingService implements IBuildingService {

	@Autowired
	private BuildingDao buildingDao;
	
	public Building findBuildingById(long id) {
		return buildingDao.findById(id);
	}
	
	public List<Building> listBuilding() {
		return buildingDao.listBuilding();
	}

}
