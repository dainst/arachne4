package de.uni_koeln.arachne.mapping.hibernate;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mapping class for the "verwaltung_benutzer_password_reset_request"-table.
 * @author Reimar Grabowski
 */
@Entity
@Table(name="verwaltung_benutzer_reset_password_request")
public class ResetPasswordRequest {

	@Id
	@GeneratedValue
	@Column(name="PS_verwaltung_benutzer_reset_password_requestID")
	private Long id = -1L;
	
	@Column(name="FS_uid")
	private Long userId;
	
	@Column(name="token")
	private String token;
	
	@Column(name="expiration_date")
	private Timestamp expirationDate;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the expirationDate
	 */
	public Timestamp getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(Timestamp expirationDate) {
		this.expirationDate = expirationDate;
	}
}
