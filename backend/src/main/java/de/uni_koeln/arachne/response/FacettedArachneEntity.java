package de.uni_koeln.arachne.response;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class contains only the facets for preformatted output to the frontend.
 * Every used facet must be defined here. Do not forget to add getters and setters.
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class FacettedArachneEntity extends BaseArachneEntity {
	/*
	 * The facets as defined in the xml file for the dataset.
	 */
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
	protected List<String> facet_buchtitel;
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
	protected List<String> facet_aufbau;
	protected List<String> facet_aufstellungskontext;
	protected List<String> facet_funktion;
	protected List<String> facet_thematik;
	protected List<String> facet_thematikmenschen;
	protected List<String> facet_archaeologzeugnisse;
	
	// gruppierung
	protected List<String> facet_inhaltlichekategorie;
	
	// individualvessel
	protected List<String> facet_vesselinventorynumber;
	
	// inschrift
	protected List<String> facet_sprache;
	
	// isolatedsherd
	protected List<String> facet_sherdtype;
	protected List<String> facet_inventorynumber;
	protected List<String> facet_nitonanalysisnummer;
	
	// mainabstract
	protected List<String> facet_grabungsinternetypnumber;
	protected List<String> facet_fabricorigin;
	
	// morphology
	protected List<String> facet_morphologymainabstractfabric;
	protected List<String> facet_morphologymainabstractbefund;
	
	// objekt
	protected List<String> facet_objektgattung;
	protected List<String> facet_applizierteelemente;
	protected List<String> facet_bearbeitung;
	protected List<String> facet_funktionaleverwendung;
	protected List<String> facet_material;
	protected List<String> facet_materialbeschreibung;
	protected List<String> facet_technik;
	protected List<String> facet_schmuckspezifizierung;
	
	// person
	protected List<String> facet_titel;
	protected List<String> facet_geschlecht;
	protected List<String> facet_ethnienationalität;
	
	// realien
	protected List<String> facet_realienart;
	protected List<String> facet_attributallg;
	protected List<String> facet_bearbeitungen;
	protected List<String> facet_bennenungallg;
	protected List<String> facet_bewaffnung;
	protected List<String> facet_dekoration;
	protected List<String> facet_wesen;

	// relief
	protected List<String> facet_erhaltung;
	protected List<String> facet_personen;
	protected List<String> facet_personengoetter;
	protected List<String> facet_personenheroen;
	protected List<String> facet_personenmenschen;
	protected List<String> facet_personenmythwesen;
	protected List<String> facet_personentiere;
	
	// reproduktion
	protected List<String> facet_ergaenzungen;
	protected List<String> facet_gattungallgemein;
	protected List<String> facet_gattungspeziell;
	protected List<String> facet_gottgoettin;
	protected List<String> facet_mischwesen;
	protected List<String> facet_grundform;

	// rezeption
	protected List<String> facet_fundstaat;
	
	// sammlungen
	protected List<String> facet_sammlungskategorie;
	protected List<String> facet_herkunftsland;
	
	// sarkophag
	protected List<String> facet_thematikFrei;
	protected List<String> facet_thematikDeckel;
	protected List<String> facet_thematikDeckelVorderseite;
	protected List<String> facet_thematikDeckelNebenseiten;
	protected List<String> facet_thematikKasten;
	protected List<String> facet_thematikKastenVorderseite;
	protected List<String> facet_thematikKastenNebenseiten;
	
	// surfacetreatmentaction
	protected List<String> facet_surfacetreatmentactionaction;
	protected List<String> facet_surfacetreatmentactionposition;
	protected List<String> facet_surfacetreatmentactionmoment;
	protected List<String> facet_surfacetreatmentactionpartofsurfacetreated;
	
	// surfacetreatment
	protected List<String> facet_surfacetreatmentbezeichner;
	
	// topographie
	protected List<String> facet_topographieart;
	protected List<String> facet_topographietypus;
	
	// typus
	protected List<String> facet_klassifizierung;
	protected List<String> facet_bekleidung;
	protected List<String> facet_bildschema;
	protected List<String> facet_haltung;
	protected List<String> facet_roemgriech;
	
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
	public List<String> getFacet_buchtitel() {
		return facet_buchtitel;
	}
	public void setFacet_buchtitel(List<String> facet_buchtitel) {
		this.facet_buchtitel = facet_buchtitel;
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
	public List<String> getFacet_aufbau() {
		return facet_aufbau;
	}
	public void setFacet_aufbau(List<String> facet_aufbau) {
		this.facet_aufbau = facet_aufbau;
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
	public List<String> getFacet_inhaltlichekategorie() {
		return facet_inhaltlichekategorie;
	}
	public void setFacet_inhaltlichekategorie(
			List<String> facet_inhaltlichekategorie) {
		this.facet_inhaltlichekategorie = facet_inhaltlichekategorie;
	}
	public List<String> getFacet_vesselinventorynumber() {
		return facet_vesselinventorynumber;
	}
	public void setFacet_vesselinventorynumber(
			List<String> facet_vesselinventorynumber) {
		this.facet_vesselinventorynumber = facet_vesselinventorynumber;
	}
	public List<String> getFacet_sprache() {
		return facet_sprache;
	}
	public void setFacet_sprache(List<String> facet_sprache) {
		this.facet_sprache = facet_sprache;
	}
	public List<String> getFacet_sherdtype() {
		return facet_sherdtype;
	}
	public void setFacet_sherdtype(List<String> facet_sherdtype) {
		this.facet_sherdtype = facet_sherdtype;
	}
	public List<String> getFacet_inventorynumber() {
		return facet_inventorynumber;
	}
	public void setFacet_inventorynumber(List<String> facet_inventorynumber) {
		this.facet_inventorynumber = facet_inventorynumber;
	}
	public List<String> getFacet_nitonanalysisnummer() {
		return facet_nitonanalysisnummer;
	}
	public void setFacet_nitonanalysisnummer(List<String> facet_nitonanalysisnummer) {
		this.facet_nitonanalysisnummer = facet_nitonanalysisnummer;
	}
	public List<String> getFacet_grabungsinternetypnumber() {
		return facet_grabungsinternetypnumber;
	}
	public void setFacet_grabungsinternetypnumber(
			List<String> facet_grabungsinternetypnumber) {
		this.facet_grabungsinternetypnumber = facet_grabungsinternetypnumber;
	}
	public List<String> getFacet_fabricorigin() {
		return facet_fabricorigin;
	}
	public void setFacet_fabricorigin(List<String> facet_fabricorigin) {
		this.facet_fabricorigin = facet_fabricorigin;
	}
	public List<String> getFacet_morphologymainabstractfabric() {
		return facet_morphologymainabstractfabric;
	}
	public void setFacet_morphologymainabstractfabric(
			List<String> facet_morphologymainabstractfabric) {
		this.facet_morphologymainabstractfabric = facet_morphologymainabstractfabric;
	}
	public List<String> getFacet_morphologymainabstractbefund() {
		return facet_morphologymainabstractbefund;
	}
	public void setFacet_morphologymainabstractbefund(
			List<String> facet_morphologymainabstractbefund) {
		this.facet_morphologymainabstractbefund = facet_morphologymainabstractbefund;
	}
	public List<String> getFacet_objektgattung() {
		return facet_objektgattung;
	}
	public void setFacet_objektgattung(List<String> facet_objektgattung) {
		this.facet_objektgattung = facet_objektgattung;
	}
	public List<String> getFacet_applizierteelemente() {
		return facet_applizierteelemente;
	}
	public void setFacet_applizierteelemente(List<String> facet_applizierteelemente) {
		this.facet_applizierteelemente = facet_applizierteelemente;
	}
	public List<String> getFacet_bearbeitung() {
		return facet_bearbeitung;
	}
	public void setFacet_bearbeitung(List<String> facet_bearbeitung) {
		this.facet_bearbeitung = facet_bearbeitung;
	}
	public List<String> getFacet_funktionaleverwendung() {
		return facet_funktionaleverwendung;
	}
	public void setFacet_funktionaleverwendung(
			List<String> facet_funktionaleverwendung) {
		this.facet_funktionaleverwendung = facet_funktionaleverwendung;
	}
	public List<String> getFacet_material() {
		return facet_material;
	}
	public void setFacet_material(List<String> facet_material) {
		this.facet_material = facet_material;
	}
	public List<String> getFacet_materialbeschreibung() {
		return facet_materialbeschreibung;
	}
	public void setFacet_materialbeschreibung(
			List<String> facet_materialbeschreibung) {
		this.facet_materialbeschreibung = facet_materialbeschreibung;
	}
	public List<String> getFacet_technik() {
		return facet_technik;
	}
	public void setFacet_technik(List<String> facet_technik) {
		this.facet_technik = facet_technik;
	}
	public List<String> getFacet_schmuckspezifizierung() {
		return facet_schmuckspezifizierung;
	}
	public void setFacet_schmuckspezifizierung(
			List<String> facet_schmuckspezifizierung) {
		this.facet_schmuckspezifizierung = facet_schmuckspezifizierung;
	}
	public List<String> getFacet_titel() {
		return facet_titel;
	}
	public void setFacet_titel(List<String> facet_titel) {
		this.facet_titel = facet_titel;
	}
	public List<String> getFacet_geschlecht() {
		return facet_geschlecht;
	}
	public void setFacet_geschlecht(List<String> facet_geschlecht) {
		this.facet_geschlecht = facet_geschlecht;
	}
	public List<String> getFacet_ethnienationalität() {
		return facet_ethnienationalität;
	}
	public void setFacet_ethnienationalität(List<String> facet_ethnienationalität) {
		this.facet_ethnienationalität = facet_ethnienationalität;
	}
	public List<String> getFacet_realienart() {
		return facet_realienart;
	}
	public void setFacet_realienart(List<String> facet_realienart) {
		this.facet_realienart = facet_realienart;
	}
	public List<String> getFacet_attributallg() {
		return facet_attributallg;
	}
	public void setFacet_attributallg(List<String> facet_attributallg) {
		this.facet_attributallg = facet_attributallg;
	}
	public List<String> getFacet_bearbeitungen() {
		return facet_bearbeitungen;
	}
	public void setFacet_bearbeitungen(List<String> facet_bearbeitungen) {
		this.facet_bearbeitungen = facet_bearbeitungen;
	}
	public List<String> getFacet_bennenungallg() {
		return facet_bennenungallg;
	}
	public void setFacet_bennenungallg(List<String> facet_bennenungallg) {
		this.facet_bennenungallg = facet_bennenungallg;
	}
	public List<String> getFacet_bewaffnung() {
		return facet_bewaffnung;
	}
	public void setFacet_bewaffnung(List<String> facet_bewaffnung) {
		this.facet_bewaffnung = facet_bewaffnung;
	}
	public List<String> getFacet_dekoration() {
		return facet_dekoration;
	}
	public void setFacet_dekoration(List<String> facet_dekoration) {
		this.facet_dekoration = facet_dekoration;
	}
	public List<String> getFacet_wesen() {
		return facet_wesen;
	}
	public void setFacet_wesen(List<String> facet_wesen) {
		this.facet_wesen = facet_wesen;
	}
	public List<String> getFacet_erhaltung() {
		return facet_erhaltung;
	}
	public void setFacet_erhaltung(List<String> facet_erhaltung) {
		this.facet_erhaltung = facet_erhaltung;
	}
	public List<String> getFacet_personen() {
		return facet_personen;
	}
	public void setFacet_personen(List<String> facet_personen) {
		this.facet_personen = facet_personen;
	}
	public List<String> getFacet_personengoetter() {
		return facet_personengoetter;
	}
	public void setFacet_personengoetter(List<String> facet_personengoetter) {
		this.facet_personengoetter = facet_personengoetter;
	}
	public List<String> getFacet_personenheroen() {
		return facet_personenheroen;
	}
	public void setFacet_personenheroen(List<String> facet_personenheroen) {
		this.facet_personenheroen = facet_personenheroen;
	}
	public List<String> getFacet_personenmenschen() {
		return facet_personenmenschen;
	}
	public void setFacet_personenmenschen(List<String> facet_personenmenschen) {
		this.facet_personenmenschen = facet_personenmenschen;
	}
	public List<String> getFacet_personenmythwesen() {
		return facet_personenmythwesen;
	}
	public void setFacet_personenmythwesen(List<String> facet_personenmythwesen) {
		this.facet_personenmythwesen = facet_personenmythwesen;
	}
	public List<String> getFacet_personentiere() {
		return facet_personentiere;
	}
	public void setFacet_personentiere(List<String> facet_personentiere) {
		this.facet_personentiere = facet_personentiere;
	}
	public List<String> getFacet_ergaenzungen() {
		return facet_ergaenzungen;
	}
	public void setFacet_ergaenzungen(List<String> facet_ergaenzungen) {
		this.facet_ergaenzungen = facet_ergaenzungen;
	}
	public List<String> getFacet_gattungallgemein() {
		return facet_gattungallgemein;
	}
	public void setFacet_gattungallgemein(List<String> facet_gattungallgemein) {
		this.facet_gattungallgemein = facet_gattungallgemein;
	}
	public List<String> getFacet_gattungspeziell() {
		return facet_gattungspeziell;
	}
	public void setFacet_gattungspeziell(List<String> facet_gattungspeziell) {
		this.facet_gattungspeziell = facet_gattungspeziell;
	}
	public List<String> getFacet_gottgoettin() {
		return facet_gottgoettin;
	}
	public void setFacet_gottgoettin(List<String> facet_gottgoettin) {
		this.facet_gottgoettin = facet_gottgoettin;
	}
	public List<String> getFacet_mischwesen() {
		return facet_mischwesen;
	}
	public void setFacet_mischwesen(List<String> facet_mischwesen) {
		this.facet_mischwesen = facet_mischwesen;
	}
	public List<String> getFacet_grundform() {
		return facet_grundform;
	}
	public void setFacet_grundform(List<String> facet_grundform) {
		this.facet_grundform = facet_grundform;
	}
	public List<String> getFacet_fundstaat() {
		return facet_fundstaat;
	}
	public void setFacet_fundstaat(List<String> facet_fundstaat) {
		this.facet_fundstaat = facet_fundstaat;
	}
	public List<String> getFacet_sammlungskategorie() {
		return facet_sammlungskategorie;
	}
	public void setFacet_sammlungskategorie(List<String> facet_sammlungskategorie) {
		this.facet_sammlungskategorie = facet_sammlungskategorie;
	}
	public List<String> getFacet_herkunftsland() {
		return facet_herkunftsland;
	}
	public void setFacet_herkunftsland(List<String> facet_herkunftsland) {
		this.facet_herkunftsland = facet_herkunftsland;
	}
	public List<String> getFacet_thematikFrei() {
		return facet_thematikFrei;
	}
	public void setFacet_thematikFrei(List<String> facet_thematikFrei) {
		this.facet_thematikFrei = facet_thematikFrei;
	}
	public List<String> getFacet_thematikDeckel() {
		return facet_thematikDeckel;
	}
	public void setFacet_thematikDeckel(List<String> facet_thematikDeckel) {
		this.facet_thematikDeckel = facet_thematikDeckel;
	}
	public List<String> getFacet_thematikDeckelVorderseite() {
		return facet_thematikDeckelVorderseite;
	}
	public void setFacet_thematikDeckelVorderseite(
			List<String> facet_thematikDeckelVorderseite) {
		this.facet_thematikDeckelVorderseite = facet_thematikDeckelVorderseite;
	}
	public List<String> getFacet_thematikDeckelNebenseiten() {
		return facet_thematikDeckelNebenseiten;
	}
	public void setFacet_thematikDeckelNebenseiten(
			List<String> facet_thematikDeckelNebenseiten) {
		this.facet_thematikDeckelNebenseiten = facet_thematikDeckelNebenseiten;
	}
	public List<String> getFacet_thematikKasten() {
		return facet_thematikKasten;
	}
	public void setFacet_thematikKasten(List<String> facet_thematikKasten) {
		this.facet_thematikKasten = facet_thematikKasten;
	}
	public List<String> getFacet_thematikKastenVorderseite() {
		return facet_thematikKastenVorderseite;
	}
	public void setFacet_thematikKastenVorderseite(
			List<String> facet_thematikKastenVorderseite) {
		this.facet_thematikKastenVorderseite = facet_thematikKastenVorderseite;
	}
	public List<String> getFacet_thematikKastenNebenseiten() {
		return facet_thematikKastenNebenseiten;
	}
	public void setFacet_thematikKastenNebenseiten(
			List<String> facet_thematikKastenNebenseiten) {
		this.facet_thematikKastenNebenseiten = facet_thematikKastenNebenseiten;
	}
	public List<String> getFacet_surfacetreatmentactionaction() {
		return facet_surfacetreatmentactionaction;
	}
	public void setFacet_surfacetreatmentactionaction(
			List<String> facet_surfacetreatmentactionaction) {
		this.facet_surfacetreatmentactionaction = facet_surfacetreatmentactionaction;
	}
	public List<String> getFacet_surfacetreatmentactionposition() {
		return facet_surfacetreatmentactionposition;
	}
	public void setFacet_surfacetreatmentactionposition(
			List<String> facet_surfacetreatmentactionposition) {
		this.facet_surfacetreatmentactionposition = facet_surfacetreatmentactionposition;
	}
	public List<String> getFacet_surfacetreatmentactionmoment() {
		return facet_surfacetreatmentactionmoment;
	}
	public void setFacet_surfacetreatmentactionmoment(
			List<String> facet_surfacetreatmentactionmoment) {
		this.facet_surfacetreatmentactionmoment = facet_surfacetreatmentactionmoment;
	}
	public List<String> getFacet_surfacetreatmentactionpartofsurfacetreated() {
		return facet_surfacetreatmentactionpartofsurfacetreated;
	}
	public void setFacet_surfacetreatmentactionpartofsurfacetreated(
			List<String> facet_surfacetreatmentactionpartofsurfacetreated) {
		this.facet_surfacetreatmentactionpartofsurfacetreated = facet_surfacetreatmentactionpartofsurfacetreated;
	}
	public List<String> getFacet_surfacetreatmentbezeichner() {
		return facet_surfacetreatmentbezeichner;
	}
	public void setFacet_surfacetreatmentbezeichner(
			List<String> facet_surfacetreatmentbezeichner) {
		this.facet_surfacetreatmentbezeichner = facet_surfacetreatmentbezeichner;
	}
	public List<String> getFacet_topographieart() {
		return facet_topographieart;
	}
	public void setFacet_topographieart(List<String> facet_topographieart) {
		this.facet_topographieart = facet_topographieart;
	}
	public List<String> getFacet_topographietypus() {
		return facet_topographietypus;
	}
	public void setFacet_topographietypus(List<String> facet_topographietypus) {
		this.facet_topographietypus = facet_topographietypus;
	}
	public List<String> getFacet_klassifizierung() {
		return facet_klassifizierung;
	}
	public void setFacet_klassifizierung(List<String> facet_klassifizierung) {
		this.facet_klassifizierung = facet_klassifizierung;
	}
	public List<String> getFacet_bekleidung() {
		return facet_bekleidung;
	}
	public void setFacet_bekleidung(List<String> facet_bekleidung) {
		this.facet_bekleidung = facet_bekleidung;
	}
	public List<String> getFacet_bildschema() {
		return facet_bildschema;
	}
	public void setFacet_bildschema(List<String> facet_bildschema) {
		this.facet_bildschema = facet_bildschema;
	}
	public List<String> getFacet_haltung() {
		return facet_haltung;
	}
	public void setFacet_haltung(List<String> facet_haltung) {
		this.facet_haltung = facet_haltung;
	}
	public List<String> getFacet_roemgriech() {
		return facet_roemgriech;
	}
	public void setFacet_roemgrich(List<String> facet_roemgrich) {
		this.facet_roemgriech = facet_roemgrich;
	}
}
