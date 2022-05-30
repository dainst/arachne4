package de.uni_koeln.arachne.context;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This contextualizer adds paths to the 3d model's files to the context.
 */
public class Modell3ddownloadsContextualizer extends AbstractContextualizer {

	private class LinkWithAddedDownloadPath extends ArachneLink {

		LinkWithAddedDownloadPath(Dataset entity, String downloadPath) {
			this.setEntity1(entity);
			this.setEntity2(null);
			Map<String, String> fields = new TreeMap<String, String>();
			fields.put("Modell3DDownloads.downloadPath", downloadPath);
			this.setFields(fields);
		}
	}

	@Override
	public String getContextType() {
		return "Modell3DDownloads";
	}

	@Override
	public List<AbstractLink> retrieve(Dataset parent) {
		final List<AbstractLink> links = new ArrayList<>(2);

		final String folder = parent.getField("modell3d.Pfad");
		if (!StrUtils.isEmptyOrNull(folder)) {
			final String model = parent.getField("modell3d.Dateiname");
			final String format = parent.getField("modell3d.Dateiformat");

			if (!StrUtils.isEmptyOrNull(model)) {
				String path = buildPath(folder, model);
				if (!StrUtils.isEmptyOrNull(format) && format.equals("objmtl")) {
					int index = path.lastIndexOf(".");
					path = path.substring(0, index) + ".zip";
				}
			 	links.add(new LinkWithAddedDownloadPath(parent, path));	
			}
		}
		return links;
	}

	private static final String buildPath(String folder, String file) {
		String path = Paths.get(folder, file).toString();
		while (path.startsWith("/")) {
			path = path.replaceFirst("/", "");
		}
		return path;
	}
}
