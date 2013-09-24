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

	private static String qualifier = "quantities.";
	
	private int rimCount = 0;

	private int handleCount = 0;

	private int baseCount = 0;

	private int bodySherdCount = 0;

	private int othersCount = 0;

	private float rimWeight = 0.0f;

	private float handleWeight = 0.0f;

	private float bodySherdWeight = 0.0f;

	private float baseWeight = 0.0f;

	private float othersWeight = 0.0f;
	
	private float totalWeight = 0.0f;

	private int mni = 0;

	private int mxi = 0;

	private float rimPercentage = 0.0f;

	private float mniWeighted = 0.0f;

	private int totalSherds = 0;
	
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
			final String content = quantification.get(qualifier + fieldName);

			if (content != null && !content.isEmpty()) {
				classMembers[i].setAccessible(true);
				final Class fieldType = classMembers[i].getType();
				try {
					final String fieldTypeString = fieldType.getName();
					if ("int".equals(fieldTypeString)) {
						classMembers[i].setInt(this, Integer.valueOf(content));
					} else if("float".equals(fieldTypeString)) {
						classMembers[i].setFloat(this, Float.valueOf(content));
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
		result.put("baseWeight", String.valueOf(baseWeight / 1000.0f));
		result.put("bodySherdCount", String.valueOf(bodySherdCount));
		result.put("bodySherdWeight", String.valueOf(bodySherdWeight / 1000.0f) );
		result.put("handleCount", String.valueOf(handleCount));
		result.put("handleWeight", String.valueOf(handleWeight / 1000.0f));
		result.put("othersCount", String.valueOf(othersCount));
		result.put("othersWeight", String.valueOf(othersWeight / 1000.0f));
		result.put("rimPercentage", String.valueOf(rimPercentage));
		result.put("rimCount", String.valueOf(rimCount));
		result.put("rimWeight", String.valueOf(rimWeight / 1000.0f));
		result.put("mni", String.valueOf(mni));
		result.put("mniWeighted", String.valueOf(mniWeighted));
		result.put("mxi", String.valueOf(mxi));
		result.put("totalSherds", String.valueOf(totalSherds));
		result.put("totalWeight", String.valueOf(totalWeight / 1000.0f));
		return result;
	}

	public void setRimCount(Integer rimCount) {
		this.rimCount = rimCount;
	}

	public void setHandleCount(Integer handleCount) {
		this.handleCount = handleCount;
	}

	public void setBaseCount(Integer baseCount) {
		this.baseCount = baseCount;
	}

	public void setBodySherdCount(Integer bodySherdCount) {
		this.bodySherdCount = bodySherdCount;
	}

	public void setOthersCount(Integer othersCount) {
		this.othersCount = othersCount;
	}

	public void setRimWeight(Float rimWeight) {
		this.rimWeight = rimWeight;
	}

	public void setHandleWeight(Float handleWeight) {
		this.handleWeight = handleWeight;
	}

	public void setBodySherdWeight(Float bodySherdWeight) {
		this.bodySherdWeight = bodySherdWeight;
	}

	public void setBaseWeight(Float baseWeight) {
		this.baseWeight = baseWeight;
	}

	public void setOthersWeight(Float othersWeight) {
		this.othersWeight = othersWeight;
	}

	public void setTotalWeight(Float totalWeight) {
		this.totalWeight = totalWeight;
	}

	public void setMni(Integer mni) {
		this.mni = mni;
	}

	public void setMxi(Integer mxi) {
		this.mxi = mxi;
	}

	public void setRimPercentage(Float rimPercentage) {
		this.rimPercentage = rimPercentage;
	}

	public void setMniWeighted(Float mniWeighted) {
		this.mniWeighted = mniWeighted;
	}

	public void setTotalSherds(Integer totalSherds) {
		this.totalSherds = totalSherds;
	}

	public void setContainsContent(Boolean containsContent) {
		this.containsContent = containsContent;
	}
	
}
