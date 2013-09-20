package de.uni_koeln.arachne.response;

import java.util.HashMap;
import java.util.Map;

/**
 * Class is used to hold single quantity-records as well as the result-object of
 * the aggregation computations
 * 
 * @author Patrick Gunia
 * 
 */

public class QuantificationContent {

	private Integer rimCount = 0;

	private Integer handleCount = 0;

	private Integer baseCount = 0;

	private Integer bodySherdCount = 0;

	private Integer othersCount = 0;

	private Float rimWeight = 0.0f;

	private Float handleWeight = 0.0f;

	private Float bodySherdWeight = 0.0f;

	private Float baseWeight = 0.0f;

	private Float othersWeight = 0.0f;
	
	private Float totalWeight = 0.0f;

	private Integer mni = 0;

	private Integer mxi = 0;

	private Float rimPercentage = 0.0f;

	private Float mniWeighted = 0.0f;

	private Integer totalSherds = 0;
	
	private Boolean containsContent = false;
	
	/**
	 * Empty default-constructor
	 */
	public QuantificationContent() {
		super();
	}

	/**
	 * Constructor takes a map containing either
	 * 
	 * @param quantification
	 *            Map-struct as it is retrieved from database
	 */
	public QuantificationContent(final Map<String, String> quantification) {
		final Class quantificationContent = this.getClass();
		final java.lang.reflect.Field[] classMembers = quantificationContent
				.getDeclaredFields();

		for (int i = 0; i < classMembers.length; i++) {
			String fieldName = classMembers[i].getName();
			fieldName = fieldName.substring(0, 1).toUpperCase()
					+ fieldName.substring(1);
			final String content = quantification.get(fieldName);

			if (content != null && !content.isEmpty()) {
				final Class fieldType = classMembers[i].getType();
				try {
					final String fieldTypeString = fieldType.getCanonicalName();
					
					if ("Integer".equals(fieldTypeString)) {
						classMembers[i].setInt(this, Integer.valueOf(content));
					} else if("Float".equals(fieldTypeString)) {
						classMembers[i].setFloat(this, Integer.valueOf(content));
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String toString() {
		return "QuantificationContent [rimCount=" + rimCount + ", handleCount="
				+ handleCount + ", baseCount=" + baseCount
				+ ", bodySherdCount=" + bodySherdCount + ", othersCount="
				+ othersCount + ", rimWeight=" + rimWeight + ", handleWeight="
				+ handleWeight + ", bodySherdWeight=" + bodySherdWeight
				+ ", baseWeight=" + baseWeight + ", othersWeight="
				+ othersWeight + ", mni=" + mni + ", mxi=" + mxi
				+ ", rimPercentage=" + rimPercentage + ", mniWeighted="
				+ mniWeighted + ", totalSherds=" + totalSherds + "]";
	}

	public Integer getRimCount() {
		return rimCount;
	}

	public Integer getHandleCount() {
		return handleCount;
	}

	public Integer getBaseCount() {
		return baseCount;
	}

	public Integer getBodySherdCount() {
		return bodySherdCount;
	}

	public Integer getOthersCount() {
		return othersCount;
	}

	public Float getRimWeight() {
		return rimWeight;
	}

	public Float getHandleWeight() {
		return handleWeight;
	}

	public Float getBodySherdWeight() {
		return bodySherdWeight;
	}

	public Float getBaseWeight() {
		return baseWeight;
	}

	public Float getOthersWeight() {
		return othersWeight;
	}

	public Integer getMni() {
		return mni;
	}

	public Integer getMxi() {
		return mxi;
	}

	public Float getRimPercentage() {
		return rimPercentage;
	}

	public Float getMniWeighted() {
		return mniWeighted;
	}

	public Integer getTotalSherds() {
		return totalSherds;
	}
	
	public Boolean getContainsContent() {
		return containsContent;
	}

	public void add(final QuantificationContent other) {
		
		this.baseCount += other.getBaseCount();
		this.totalSherds += this.baseCount;
		this.baseWeight += other.getBaseWeight();
		this.totalWeight += this.baseWeight;
		
		this.bodySherdCount += other.getBodySherdCount();
		this.totalSherds += this.bodySherdCount;
		this.bodySherdWeight += other.getBodySherdWeight();
		this.totalWeight += this.bodySherdWeight;
		
		this.handleCount += other.getHandleCount();
		this.totalSherds += this.handleCount;
		this.handleWeight += other.getHandleWeight();
		this.totalWeight += this.handleWeight;
		
		this.othersCount += other.getOthersCount();
		this.totalSherds += this.othersCount;
		this.othersWeight += other.getOthersWeight();
		this.totalWeight += this.othersWeight;
		
		this.rimPercentage += other.getRimPercentage();
		this.rimCount += other.getRimCount();
		this.totalSherds += this.rimCount;
		this.rimWeight += other.getRimWeight();
		this.totalWeight += this.rimWeight;
		
		this.mni += other.getMni();
		this.mniWeighted += other.getMniWeighted();
		this.mxi += other.getMxi();
	}
	
	public Map<String, String> getAsMap() {
		final Map<String, String> result = new HashMap<String, String>();
		result.put("containsContent", String.valueOf(containsContent));
		result.put("baseCount", String.valueOf(baseCount));
		result.put("baseWeight", String.valueOf(baseWeight));
		result.put("bodySherdCount", String.valueOf(bodySherdCount));
		result.put("bodySherdWeight", String.valueOf(bodySherdWeight));
		result.put("handleCount", String.valueOf(handleCount));
		result.put("handleWeight", String.valueOf(handleWeight));
		result.put("othersCount", String.valueOf(othersCount));
		result.put("othersWeight", String.valueOf(othersWeight));
		result.put("rimPercentage", String.valueOf(rimPercentage));
		result.put("rimCount", String.valueOf(rimCount));
		result.put("rimWeight", String.valueOf(rimWeight));
		result.put("mni", String.valueOf(mni));
		result.put("mniWeighted", String.valueOf(mniWeighted));
		result.put("mxi", String.valueOf(mxi));
		result.put("totalSherds", String.valueOf(totalSherds));
		result.put("totalWeight", String.valueOf(totalWeight));
		return result;
	}
	
}
