package net.lacnic.portal.auth.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoginData implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean authenticated;
	private String username;
	private List<String> roles;
	private String error;

	public LoginData() {
		this("");
	}

	public LoginData(String error) {
		this.authenticated = false;
		this.roles = new ArrayList<String>();
		this.username = "";
		this.error = error;

	}

	public LoginData(List<String> roles) {
		this.authenticated = !roles.isEmpty();
		this.roles = roles;
	}

	public LoginData(List<String> roles, String username) {
		this.authenticated = !roles.isEmpty();
		this.roles = roles;
		this.username = username;
		this.error = authenticated ? "" : "Error: verifique usuario y/o contraseña";
	}

	public boolean getAuthenticated() {
		return authenticated;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "LDAPData [authenticated=" + authenticated + ", roles=" + roles + ", email=" + username + "]";
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
