package de.uni_koeln.arachne.mapping;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="verwaltung_sessions_arachne4")
public class Session {

	@Id
	private String sid;
	@OneToOne
	@JoinColumn(name="uid")
	private UserAdministration user; // NOPMD
	private Date timestamp;
	private String ipaddress;
	private String useragent;
	/**
	 * @return the sid
	 */
	public String getSid() {
		return sid;
	}
	/**
	 * @param sid the sid to set
	 */
	public void setSid(String sid) {
		this.sid = sid;
	}
	/**
	 * @return the user
	 */
	public UserAdministration getUserAdmistration() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUserAdministration(UserAdministration user) {
		this.user = user;
	}
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the ipaddress
	 */
	public String getIpaddress() {
		return ipaddress;
	}
	/**
	 * @param ipaddress the ipaddress to set
	 */
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	/**
	 * @return the useragent
	 */
	public String getUseragent() {
		return useragent;
	}
	/**
	 * @param useragent the useragent to set
	 */
	public void setUseragent(String useragent) {
		this.useragent = useragent;
	}
	
}
