package de.uni_koeln.arachne.service;

import java.util.List;

import de.uni_koeln.arachne.mapping.Building;

public interface IBuildingService {
	public Building findBuildingById(long id);
	public List<Building> listBuilding();
}
