package de.uni_koeln.arachne.config;

import java.util.Arrays;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

import de.uni_koeln.arachne.response.link.SimpleExternalLinkResolver;

/**
 * Configuration for the <code>ExternalLinkResolver</code> beans.
 *
 * @author Reimar Grabowski
 */
@Configuration
public class ExternalLinkResolvers {

	/**
	 * Resolver for SVG viewer links.
	 *
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver viewerSVG() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("SVG Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "modell3d", "modell3d.Dateiformat", "svg");
		result.setCriteria(criteria);
		result.setMatchAllCriteria(true);
		result.setLinkPattern("https://arachne.dainst.org/svg?id=%s");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}

	/**
	 * Resolver for 3D model viewer links (obj format).
	 *
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver viewer3D() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("3D-Modell Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "modell3d", "modell3d.Dateiformat", "obj");
		result.setCriteria(criteria);
		result.setMatchAllCriteria(true);
		result.setLinkPattern("https://arachne.dainst.org/3d?id=%s");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}

	/**
	 * Resolver for 3D model viewer links (objmtl format).
	 *
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver viewer3Dmtl() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("3D-Modell Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "modell3d", "modell3d.Dateiformat", "objmtl");
		result.setCriteria(criteria);
		result.setMatchAllCriteria(true);
		result.setLinkPattern("https://arachne.dainst.org/3d?id=%s");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}

	/**
	 * Resolver for 3D model viewer links (nxz format).
	 *
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver viewer3Dnxz() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("3D-Modell Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "modell3d", "modell3d.Dateiformat", "nxz");
		result.setCriteria(criteria);
		result.setMatchAllCriteria(true);
		result.setLinkPattern("https://arachne.dainst.org/3dhop/full.html?model=/data/model/%s.nxz");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}

	/**
	 * Resolver for TEI-viewer links (books).
	 *
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver teiViewerBook() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("TEI-Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "buch");
		result.setCriteria(criteria);
		result.setLinkPattern("https://objects.auxiliary.idai.world/Tei-Viewer/cgi-bin/teiviewer.php?manifest=%s");
		result.setValidationPattern("https://objects.auxiliary.idai.world/TeiDocuments/%s/structure.xml");
		result.setPatternFields(Arrays.asList("buch.Verzeichnis"));
		return result;
	}

	/**
	 * Resolver for DFG-viewer links.
	 *
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver dfgViewer() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("DFG-Viewer");
        result.setDatasetGroup("Arachne");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "buch");
		result.setCriteria(criteria);
		result.setLinkPattern("http://dfg-viewer.de/show/"
				+ "?set[mets]=http%%3A%%2F%%2Fobjects.auxiliary.idai.world%%2FMetsDocuments%%2F"
				+ "oai_arachne.uni-koeln.de_buch-%s.xml");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}


	/**
	 * Resolver for bookviewer links from the gelehrtenbriefe project.
	 *
	 * @return The Link resolver
	 */
	// NOTE: It is unclear to what extent the book viewer will be used in the
	// future. For this reason we did not attempt a more general solution.
	@Bean
	public SimpleExternalLinkResolver bookviewerGelehrtenbriefe() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Book-Viewer");
		final Map<String, String> criteria = ImmutableMap.of("buch.ArbeitsnotizBuch", "TranskriptionGelehrtenbriefe");
		result.setCriteria(criteria);
		result.setExactMatch(false);
		// We cannot decide if annotations are present, so just include the param
		result.setLinkPattern("https://viewer.idai.world/?file=data/gelehrtenbriefe/%s.pdf&pubid=annotations/%s.json");
		result.setPatternFields(Arrays.asList("buch.bibid", "buch.bibid"));
		return result;
	}
}
