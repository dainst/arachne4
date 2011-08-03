package de.uni_koeln.arachne.dao;

import java.util.List;

import de.uni_koeln.arachne.mapping.Building;

public interface IBuildingDAO {
	public Building findById(long id);
	public List<Building> listBuilding();
}
