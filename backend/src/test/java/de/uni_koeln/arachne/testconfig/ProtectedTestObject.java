package de.uni_koeln.arachne.testconfig;

import com.fasterxml.jackson.annotation.JsonView;

import de.uni_koeln.arachne.util.security.JSONView;
import de.uni_koeln.arachne.util.security.ProtectedObject;
import de.uni_koeln.arachne.util.security.UserAccess;

public class ProtectedTestObject extends ProtectedObject {

	@JsonView(JSONView.Admin.class)
	@UserAccess(UserAccess.Restrictions.writeprotected)
	private String writeProtectedStringValue;
	
	@JsonView(JSONView.User.class)
	private String userStringValue; 
	
	@JsonView(JSONView.Admin.class)
	private String adminStringValue;
	
	public ProtectedTestObject() {
		writeProtectedStringValue = "cannot be changed";
		userStringValue = "changeable by user";
		adminStringValue = "changeable by admin";
	}

	/**
	 * @return the writeProtectedStringValue
	 */
	public String getWriteProtectedStringValue() {
		return writeProtectedStringValue;
	}

	/**
	 * @param writeProtectedStringValue the writeProtectedStringValue to set
	 */
	public void setWriteProtectedStringValue(String writeProtectedStringValue) {
		this.writeProtectedStringValue = writeProtectedStringValue;
	}

	/**
	 * @return the userStringValue
	 */
	public String getUserStringValue() {
		return userStringValue;
	}

	/**
	 * @param userStringValue the userStringValue to set
	 */
	public void setUserStringValue(String userStringValue) {
		this.userStringValue = userStringValue;
	}

	/**
	 * @return the adminStringValue
	 */
	public String getAdminStringValue() {
		return adminStringValue;
	}

	/**
	 * @param adminStringValue the adminStringValue to set
	 */
	public void setAdminStringValue(String adminStringValue) {
		this.adminStringValue = adminStringValue;
	}

}
