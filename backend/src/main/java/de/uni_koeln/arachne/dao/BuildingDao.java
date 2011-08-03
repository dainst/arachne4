package de.uni_koeln.arachne.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.Building;

@Repository("buildingDao")
public class BuildingDao extends AbstractDao{

	public Building findById(long id) {
		return (Building)hibernateTemplate.get(Building.class, id);
	}
	
	public List<Building> listBuilding() {
		return hibernateTemplate.find("from Building");
	}

}
