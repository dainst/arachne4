package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Response object class that returns preformatted output to the frontend.
 * This class is serialized to JSON using <code>Jackson</code>.
 */
@XmlRootElement(name="entity")
@XmlSeeAlso({Section.class,Field.class,FieldList.class})
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class FormattedArachneEntity extends BaseArachneEntity {
	
	/**
	 * The Title of the Dataset
	 */
	protected String title;
	
	/**
	 * The Subtitle of the Dataset
	 */
	protected String subtitle;
	
	/**
	 * Hierachical structured information of the dataset.
	 */
	protected AbstractContent sections;
	
	/**
	 * The date of the last Modification of the dataset.
	 */
	protected Date lastModified;
	
	/**
	 * The context map Contains the Contexts of the dataset.
	 */
	protected AbstractContent context;
	
	/**
	 * The Images that are associated with the dataset
	 */
	protected List<Image> images;
	
	/**
	 * The image id of the thumbnail of the dataset
	 */
	protected Long thumbnailId; 
	
	/**
	 * The facets as defined in the xml file for the dataset.
	 */
	//protected List<Facet> facets;
	
	// facet fields
	
	protected List<String> facet_kategorie;
	protected List<String> facet_ort;
	protected List<String> facet_datierungepoche;
	
	// bauwerk
	protected List<String> facet_gebaeudetyp;
	protected List<String> facet_gebaeudetypspeziell;
	protected List<String> facet_kontext;
	protected List<String> facet_bauordnung;
	protected List<String> facet_kulturkreis;
	protected List<String> facet_antikegriechlandschaft;
	protected List<String> facet_antikeroemprovinz;
	protected List<String> facet_regioromitalien;
	
	// bauwerksteil
	protected List<String> facet_dekorationsart;
	
	// befund
	protected List<String> facet_grabungsort;
	protected List<String> facet_befund;
	protected List<String> facet_befundmainabstractmorphology;
	
	// buch
	protected List<String> facet_autor;
	protected List<String> facet_titel;
	protected List<String> facet_jahr;
	protected List<String> facet_schlagwort;
	
	// fabricdescription
	protected List<String> facet_einschlusstyp;
	protected List<String> facet_einschlussform;
	
	// fabric
	protected List<String> facet_fabricmainabstractcontext;
	protected List<String> facet_fabricmainabstractmorphology;
	protected List<String> facet_fabriccommonname;
	protected List<String> facet_fabricname;
	
	// gruppen
	protected List<String> facet_artdergruppe;
	protected List<String> facet_aufstellungskontext;
	protected List<String> facet_funktion;
	protected List<String> facet_thematik;
	protected List<String> facet_thematikmenschen;
	protected List<String> facet_archaeologzeugnisse;
	
	// gruppierung
	
	/**
	 * Parameterless constructor initializing title and subtitle.
	 */
	public FormattedArachneEntity() {
		title = "";
		subtitle = "";
		//facets = new ArrayList<Facet>(); 
	}	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(final String subtitle) {
		this.subtitle = subtitle;
	}
	
	public AbstractContent getSections() {
		return sections;
	}

	public void setSections(final AbstractContent content) {
		sections = content;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(final Date lastModified) {
		this.lastModified = lastModified;
	}

	public AbstractContent getContext() {
		return context;
	}

	public void setContext(final AbstractContent context) {
		this.context = context;
	}
	
	public List<Image> getImages() {
		return images;
	}
	
	public Long getThumbnailId() {
		return thumbnailId;
	}

	/*public List<Facet> getFacets() {
		return this.facets;
	}*/
	
	public void setImages(final List<Image> images) {
		this.images = images;
	}
	
	public void setThumbnailId(final Long thumbnailId) {
		this.thumbnailId = thumbnailId;
	}
	
	/*public void setFacets(final List<Facet> facets) {
		this.facets = facets;
	}*/
	
	// Convenience function to add a facet
	/*public void addFacet(final Facet facet) {
		this.facets.add(facet);
	}*/
	
	public List<String> getFacet_kategorie() {
		return facet_kategorie;
	}

	public void setFacet_kategorie(List<String> facet_kategorie) {
		this.facet_kategorie = facet_kategorie;
	}

	public List<String> getFacet_ort() {
		return facet_ort;
	}

	public void setFacet_ort(List<String> facet_ort) {
		this.facet_ort = facet_ort;
	}

	public List<String> getFacet_datierungepoche() {
		return facet_datierungepoche;
	}

	public void setFacet_datierungepoche(List<String> facet_datierungepoche) {
		this.facet_datierungepoche = facet_datierungepoche;
	}

	public List<String> getFacet_gebaeudetyp() {
		return facet_gebaeudetyp;
	}

	public void setFacet_gebaeudetyp(List<String> facet_gebaeudetyp) {
		this.facet_gebaeudetyp = facet_gebaeudetyp;
	}

	public List<String> getFacet_gebaeudetypspeziell() {
		return facet_gebaeudetypspeziell;
	}

	public void setFacet_gebaeudetypspeziell(List<String> facet_gebaeudetypspeziell) {
		this.facet_gebaeudetypspeziell = facet_gebaeudetypspeziell;
	}

	public List<String> getFacet_kontext() {
		return facet_kontext;
	}

	public void setFacet_kontext(List<String> facet_kontext) {
		this.facet_kontext = facet_kontext;
	}

	public List<String> getFacet_bauordnung() {
		return facet_bauordnung;
	}

	public void setFacet_bauordnung(List<String> facet_bauordnung) {
		this.facet_bauordnung = facet_bauordnung;
	}

	public List<String> getFacet_kulturkreis() {
		return facet_kulturkreis;
	}

	public void setFacet_kulturkreis(List<String> facet_kulturkreis) {
		this.facet_kulturkreis = facet_kulturkreis;
	}

	public List<String> getFacet_antikegriechlandschaft() {
		return facet_antikegriechlandschaft;
	}

	public void setFacet_antikegriechlandschaft(
			List<String> facet_antikegriechlandschaft) {
		this.facet_antikegriechlandschaft = facet_antikegriechlandschaft;
	}

	public List<String> getFacet_antikeroemprovinz() {
		return facet_antikeroemprovinz;
	}

	public void setFacet_antikeroemprovinz(List<String> facet_antikeroemprovinz) {
		this.facet_antikeroemprovinz = facet_antikeroemprovinz;
	}

	public List<String> getFacet_regioromitalien() {
		return facet_regioromitalien;
	}

	public void setFacet_regioromitalien(List<String> facet_regioromitalien) {
		this.facet_regioromitalien = facet_regioromitalien;
	}

	public List<String> getFacet_dekorationsart() {
		return facet_dekorationsart;
	}

	public void setFacet_dekorationsart(List<String> facet_dekorationsart) {
		this.facet_dekorationsart = facet_dekorationsart;
	}

	public List<String> getFacet_grabungsort() {
		return facet_grabungsort;
	}

	public void setFacet_grabungsort(List<String> facet_grabungsort) {
		this.facet_grabungsort = facet_grabungsort;
	}

	public List<String> getFacet_befund() {
		return facet_befund;
	}

	public void setFacet_befund(List<String> facet_befund) {
		this.facet_befund = facet_befund;
	}

	public List<String> getFacet_befundmainabstractmorphology() {
		return facet_befundmainabstractmorphology;
	}

	public void setFacet_befundmainabstractmorphology(
			List<String> facet_befundmainabstractmorphology) {
		this.facet_befundmainabstractmorphology = facet_befundmainabstractmorphology;
	}

	public List<String> getFacet_autor() {
		return facet_autor;
	}

	public void setFacet_autor(List<String> facet_autor) {
		this.facet_autor = facet_autor;
	}

	public List<String> getFacet_titel() {
		return facet_titel;
	}

	public void setFacet_titel(List<String> facet_titel) {
		this.facet_titel = facet_titel;
	}

	public List<String> getFacet_jahr() {
		return facet_jahr;
	}

	public void setFacet_jahr(List<String> facet_jahr) {
		this.facet_jahr = facet_jahr;
	}

	public List<String> getFacet_schlagwort() {
		return facet_schlagwort;
	}

	public void setFacet_schlagwort(List<String> facet_schlagwort) {
		this.facet_schlagwort = facet_schlagwort;
	}

	public List<String> getFacet_einschlusstyp() {
		return facet_einschlusstyp;
	}

	public void setFacet_einschlusstyp(List<String> facet_einschlusstyp) {
		this.facet_einschlusstyp = facet_einschlusstyp;
	}

	public List<String> getFacet_einschlussform() {
		return facet_einschlussform;
	}

	public void setFacet_einschlussform(List<String> facet_einschlussform) {
		this.facet_einschlussform = facet_einschlussform;
	}

	public List<String> getFacet_fabricmainabstractcontext() {
		return facet_fabricmainabstractcontext;
	}

	public void setFacet_fabricmainabstractcontext(
			List<String> facet_fabricmainabstractcontext) {
		this.facet_fabricmainabstractcontext = facet_fabricmainabstractcontext;
	}

	public List<String> getFacet_fabricmainabstractmorphology() {
		return facet_fabricmainabstractmorphology;
	}

	public void setFacet_fabricmainabstractmorphology(
			List<String> facet_fabricmainabstractmorphology) {
		this.facet_fabricmainabstractmorphology = facet_fabricmainabstractmorphology;
	}

	public List<String> getFacet_fabriccommonname() {
		return facet_fabriccommonname;
	}

	public void setFacet_fabriccommonname(List<String> facet_fabriccommonname) {
		this.facet_fabriccommonname = facet_fabriccommonname;
	}

	public List<String> getFacet_fabricname() {
		return facet_fabricname;
	}

	public void setFacet_fabricname(List<String> facet_fabricname) {
		this.facet_fabricname = facet_fabricname;
	}

	public List<String> getFacet_artdergruppe() {
		return facet_artdergruppe;
	}

	public void setFacet_artdergruppe(List<String> facet_artdergruppe) {
		this.facet_artdergruppe = facet_artdergruppe;
	}

	public List<String> getFacet_aufstellungskontext() {
		return facet_aufstellungskontext;
	}

	public void setFacet_aufstellungskontext(List<String> facet_aufstellungskontext) {
		this.facet_aufstellungskontext = facet_aufstellungskontext;
	}

	public List<String> getFacet_funktion() {
		return facet_funktion;
	}

	public void setFacet_funktion(List<String> facet_funktion) {
		this.facet_funktion = facet_funktion;
	}

	public List<String> getFacet_thematik() {
		return facet_thematik;
	}

	public void setFacet_thematik(List<String> facet_thematik) {
		this.facet_thematik = facet_thematik;
	}

	public List<String> getFacet_thematikmenschen() {
		return facet_thematikmenschen;
	}

	public void setFacet_thematikmenschen(List<String> facet_thematikmenschen) {
		this.facet_thematikmenschen = facet_thematikmenschen;
	}

	public List<String> getFacet_archaeologzeugnisse() {
		return facet_archaeologzeugnisse;
	}

	public void setFacet_archaeologzeugnisse(List<String> facet_archaeologzeugnisse) {
		this.facet_archaeologzeugnisse = facet_archaeologzeugnisse;
	}
}
