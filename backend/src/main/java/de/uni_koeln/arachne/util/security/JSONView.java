package de.uni_koeln.arachne.util.security;

/**
 * Empty class used for the View feature of Jackson.
 * 
 * @author Reimar Grabowski
 *
 */
public class JSONView {
	public static class UnprivilegedUser {}
	public static class User extends UnprivilegedUser{}
	public static class Admin extends User{}
}
