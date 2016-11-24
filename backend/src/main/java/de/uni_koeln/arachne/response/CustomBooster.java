package de.uni_koeln.arachne.response;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Class to change the boost value of an entity.
 * @author Reimar Grabowski
 *
 */
@Service
public class CustomBooster {
	
	private final Map<String, Double> categoryBoosts;
	
	private final Map<Long, Double> singleEntityBoosts;
	
	/**
	 * Setter initializing the custom boost values.
	 */
	public CustomBooster() {
		categoryBoosts = new HashMap<>();
		categoryBoosts.put("marbilder", 0.1);
		categoryBoosts.put("buchseite", 0.1);
		categoryBoosts.put("ort", 0.5);
		
		singleEntityBoosts = new HashMap<>();
		// AAArC-Topographien
		singleEntityBoosts.put(5485151L, 0.5);
		singleEntityBoosts.put(5485239L, 0.5);
	}
	
	/**
	 * Get the custom boost factor for a given category.
	 * @param type The type.
	 * @return The custom boost factor for this type.
	 */
	public double getCategoryBoost(final String type) {
		if (categoryBoosts.containsKey(type)) {
			return categoryBoosts.get(type);
		}
		return 1.0D;
	}
	
	/**
	 * Get the custom boost factor for a given entity id.
	 * @param entityId The id.
	 * @return The custom boost factor for this entity.
	 */
	public double getSingleEntityBoosts(final long entityId) {
		if (singleEntityBoosts.containsKey(entityId)) {
			return singleEntityBoosts.get(entityId);
		}
		return 1.0D;
	}
}
