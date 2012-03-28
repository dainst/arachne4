package de.uni_koeln.arachne.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import de.uni_koeln.arachne.service.ImageResolutionType;

@Entity
@Table(name="verwaltung_bildrechtegruppen")
public class ImageRightsGroup {

	@Id
	private String bgid;
	@Column(name="resolution_anonymous")
	private ImageResolutionType resolutionAnonymous;
	@Column(name="watermark_anonymous")
	private String watermarkAnonymous;
	@Column(name="resolution_registered")
	private ImageResolutionType resolutionRegistered;
	@Column(name="watermark_registered")
	private String watermarkRegistered;
	@Column(name="override_for_group")
	private String overrideForGroup;

	/**
	 * @return the bgid
	 */
	public String getBgid() {
		return bgid;
	}
	/**
	 * @param bgid the bgid to set
	 */
	public void setBgid(final String bgid) {
		this.bgid = bgid;
	}
	/**
	 * @return the resolutionAnonymous
	 */
	public ImageResolutionType getResolutionAnonymous() {
		return resolutionAnonymous;
	}
	/**
	 * @param resolutionAnonymous the resolutionAnonymous to set
	 */
	public void setResolutionAnonymous(final ImageResolutionType resolutionAnonymous) {
		this.resolutionAnonymous = resolutionAnonymous;
	}
	/**
	 * @return the watermarkAnonymous
	 */
	public String getWatermarkAnonymous() {
		return watermarkAnonymous;
	}
	/**
	 * @param watermarkAnonymous the watermarkAnonymous to set
	 */
	public void setWatermarkAnonymous(final String watermarkAnonymous) {
		this.watermarkAnonymous = watermarkAnonymous;
	}
	/**
	 * @return the resolutionRegistered
	 */
	public ImageResolutionType getResolutionRegistered() {
		return resolutionRegistered;
	}
	/**
	 * @param resolutionRegistered the resolutionRegistered to set
	 */
	public void setResolutionRegistered(final ImageResolutionType resolutionRegistered) {
		this.resolutionRegistered = resolutionRegistered;
	}
	/**
	 * @return the watermarkRegistered
	 */
	public String getWatermarkRegistered() {
		return watermarkRegistered;
	}
	/**
	 * @param watermarkRegistered the watermarkRegistered to set
	 */
	public void setWatermarkRegistered(final String watermarkRegistered) {
		this.watermarkRegistered = watermarkRegistered;
	}
	/**
	 * @return the overrideForGroup
	 */
	public String getOverrideForGroup() {
		return overrideForGroup;
	}
	/**
	 * @param overrideForGroup the overrideForGroup to set
	 */
	public void setOverrideForGroup(final String overrideForGroup) {
		this.overrideForGroup = overrideForGroup;
	}
	
}
