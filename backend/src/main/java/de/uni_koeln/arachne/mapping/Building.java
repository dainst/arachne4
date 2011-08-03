package de.uni_koeln.arachne.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="bauwerk")
public class Building {
	@Id
	@Column(name="PS_BauwerkID")
	private long id;

	@Column(name="DatensatzGruppeBauwerk")
	private String entityGroupBuilding;
		
	@Column(name="Architekt")
	private String architect;
	
	@Column(name="Ausgrabung")
	private String excavation;
	
	@Column(name="Bauordnung")
	private String buildingRegulation;
	
	@Column(name="BauordnungBemerkung")
	private String buildingRegulationComment;
	
	@Column(name="BearbeiterBauwerk")
	private String editorBuilding;
	
	@Column(name="KurzbeschreibungBauwerk")
	private String shortDescription;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEntityGroupBuilding() {
		return entityGroupBuilding;
	}

	public void setEntityGroupBuilding(String entityGroupBuilding) {
		this.entityGroupBuilding = entityGroupBuilding;
	}

	public String getArchitect() {
		return architect;
	}

	public void setArchitect(String architect) {
		this.architect = architect;
	}

	public String getExcavation() {
		return excavation;
	}

	public void setExcavation(String excavation) {
		this.excavation = excavation;
	}

	public String getBuildingRegulation() {
		return buildingRegulation;
	}

	public void setBuildingRegulation(String buildingRegulation) {
		this.buildingRegulation = buildingRegulation;
	}

	public String getBuildingRegulationComment() {
		return buildingRegulationComment;
	}

	public void setBuildingRegulationComment(String buildingRegulationComment) {
		this.buildingRegulationComment = buildingRegulationComment;
	}

	public String getEditorBuilding() {
		return editorBuilding;
	}

	public void setEditorBuilding(String editorBuilding) {
		this.editorBuilding = editorBuilding;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}	
	
}
