package net.lacnic.portal.auth.client;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_INVALID_CREDENTIALS;

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
		setAuthenticated(false);
		setRoles(new ArrayList<>());
		setUsername("");
		setError(error);
	}

	public LoginData(List<String> roles) {
		setAuthenticated(!roles.isEmpty());
		setRoles(roles);
	}

	public LoginData(List<String> roles, String username) {
		setAuthenticated(!roles.isEmpty());
		setRoles(roles);
		setUsername(username);
		setError(authenticated ? "" : ERROR_INVALID_CREDENTIALS);
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public boolean getAuthenticated() {
		return isAuthenticated();
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

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "LDAPData [authenticated=" + authenticated + ", roles=" + roles + ", email=" + username + "]";
	}
}
