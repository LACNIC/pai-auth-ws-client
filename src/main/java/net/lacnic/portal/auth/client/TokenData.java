package net.lacnic.portal.auth.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TokenData implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean authenticated;
	private String token;
	private List<String> roles;
	private String error;
	private String ipAllowed;

	public TokenData() {
		this("");
	}

	public TokenData(String error) {
		this.authenticated = false;
		this.roles = new ArrayList<String>();
		this.token = "";
		this.ipAllowed = "";
		this.error = error;
	}

	public TokenData(List<String> roles, String token, String ipAllowed) {
		this.authenticated = !roles.isEmpty();
		this.roles = roles;
		this.token = token;
		this.ipAllowed = ipAllowed;
		this.error = authenticated ? "" : "No existen roles asociados a este token";
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

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getIpAllowed() {
		return ipAllowed;
	}

	public void setIpAllowed(String ipAllowed) {
		this.ipAllowed = ipAllowed;
	}

	@Override
	public String toString() {
		return "{ authenticated:\"" + authenticated + "\", token:\"" + token + "\", roles:\"" + roles + "\", error:\"" + error + "\", ipAllowed:\"" + ipAllowed + "}";
	}

}
