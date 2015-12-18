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
	 * Resolver for 3D model viewer links.
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver viewer3D() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("3D-Modell Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "modell3d");
		result.setCriteria(criteria);
		result.setLinkPattern("/3d?id=%s");
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
		result.setLinkPattern("http://arachne.uni-koeln.de/Tei-Viewer/cgi-bin/teiviewer.php?manifest=%s");
		result.setPatternFields(Arrays.asList("buch.Verzeichnis"));
		return result;
	}
	
	/**
	 * Resolver for TEI-viewer links (books).
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver teiViewerPage() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("TEI-Viewer");
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "buchseite");
		result.setCriteria(criteria);
		result.setLinkPattern("http://arachne.uni-koeln.de/Tei-Viewer/cgi-bin/teiviewer.php?scan=%s");
		result.setPatternFields(Arrays.asList("Thumbnail.Subtitle"));
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
		final Map<String, String> criteria = ImmutableMap.of("Dataset.TableName", "buch");
		result.setCriteria(criteria);
		result.setLinkPattern("http://dfg-viewer.de/show/"
				+ "?set[mets]=http%%3A%%2F%%2Farachne.uni-koeln.de%%2FMetsDocuments%%2F"
				+ "oai_arachne.uni-koeln.de_buch-%s.xml");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}
	
	/**
	 * Resolver for Sarkophagbrowser links.
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver sarkophagBrowser() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Sarkophagbrowser");
		final Map<String, String> criteria = ImmutableMap.of(
				"objekt.GattungAllgemein", "Sarkophag",
				"gruppen.ArtDerGruppe", "Sarkophaggruppe");
		result.setCriteria(criteria);
		result.setLinkPattern("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item"
				+ "&sarkophag[jump_to_project]=%s&sarkophag[jump_to_project_id]=%s");
		result.setPatternFields(Arrays.asList("Dataset.TableName", "Dataset.internalId"));
		result.setExactMatch(false);
		return result;
	}
	
	/**
	 * Resolver for Pergamon Altar browser links.
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver pergamonAltarBrowser() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Monumentbrowser zum Pergamonaltar");
		final Map<String, String> criteria = ImmutableMap.of(
				"bauerk.PS_BauwerkID", "2103257",
				"gruppen.PS_GruppenID", "402512,402414",
				"objekt.PS_ObjektID", "34644,40763",
				"relief.FS_ObjektID", "34644,40763",
				"realien.FS_ObjektID", "34644,40763");
		result.setCriteria(criteria);
		result.setLinkPattern("http://arachne.uni-koeln.de/browser/index.php?view[layout]=pergamonaltar&"
				+ "jump_to_id=%s&category=%s");
		result.setPatternFields(Arrays.asList("Dataset.internalId", "Dataset.TableName"));
		return result;
	}
	
	/**
	 * Resolver for Siegel browser links.
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver siegelBrowser() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Siegelbrowser");
		final Map<String, String> criteria = ImmutableMap.of("objektsiegel.CMSNR", "");
		result.setCriteria(criteria);
		result.setLinkPattern("http://arachne.uni-koeln.de/browser/index.php?view[layout]=siegel_item&"
				+ "objektsiegel[jump_to_id]=%s");
		result.setPatternFields(Arrays.asList("Dataset.internalId"));
		return result;
	}
	
	/**
	 * Resolver for trajan column browser links.
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver trajanColumnBrowser() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Monumentbrowser zur Trajanssäule");
		final Map<String, String> criteria = ImmutableMap.of(
				"topographie.PS_TopgraphieID", "500033",
				"bauwerk.PS_BauwerkID", "2100089",
				"objekt.PS_ObjektID", "30014,29837,130902",
				"relief.FS_ObjektID", "30014,29837,130902");
		result.setCriteria(criteria);
		// TODO find mechanism for retrieving correct value for parameter 'relief_nr'
		result.setLinkPattern("http://arachne.uni-koeln.de/browser/?view[layout]=Trajan_item&relief_nr=01");
		return result;
	}
	
	/**
	 * Resolver for Mercator browser links.
	 * 
	 * @return The link resolver.
	 */
	@Bean
	public SimpleExternalLinkResolver mercatorBrowser() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Mercatorbrowser");
		// TODO better criteria (Arachne 3 has hardcoded IDs, thus matches more often)
		final Map<String, String> criteria = ImmutableMap.of(
				"objekt.HerkFundKommentar", "Mercator",
				"reproduktion.KuenstlerReproduktion", "Mercator");
		result.setCriteria(criteria);
		result.setLinkPattern("http://arachne.uni-koeln.de/browser/index.php?view[layout]=mercator");
		result.setExactMatch(false);
		return result;
	}
	
	/**
	 * Resolver for Maffeiano browser links.
	 * 
	 * @return The link resolver.
	 */
	// TODO: not working since criteria cannot be found in the context
	@Bean
	public SimpleExternalLinkResolver maffeianoBrowser() {
		final SimpleExternalLinkResolver result = new SimpleExternalLinkResolver();
		result.setLabel("Maffeiano Stichwerk");
		final Map<String, String> criteria = ImmutableMap.of(
				"literatur.PS_LiteraturID", "69",
				"literaturzitat.FS_WebseiteID", "");
		result.setCriteria(criteria);
		result.setLinkPattern("http://arachne.uni-koeln.de/browser/maffeiano_index.php?view[layout]=Maffeiano_page&"
				+ "Maffeiano[search][PS_WebseiteID]=%s");
		result.setPatternFields(Arrays.asList("literaturzitat.FS_WebseiteID"));
		result.setMatchAllCriteria(true);
		return result;
	}
	
	// TODO implement Clarac browser link resolver
	// is the link pattern correct?
	/*<!-- resolver for links to the clarac browser -->
	<!-- TODO: not working since criteria cannot be found in the context (why?) -->
	<bean class="de.uni_koeln.arachne.response.link.SimpleExternalLinkResolver">
		<property name="label" value="Maffeiano Stichwerk" />
		<property name="criteria">
			<map>
				<entry key="literatur.PS_LiteraturID" value="65,66,67,68" />
			</map>
		</property>
		<property name="linkPattern" value="http://arachne.uni-koeln.de/browser/maffeiano_index.php?view[layout]=Maffeiano_page&Maffeiano[search][PS_WebseiteID]=%s" />
		<property name="patternFields">
			<list>
				<value>literaturzitat.FS_WebseiteID</value>	
			</list>
		</property>
	</bean>*/
}
