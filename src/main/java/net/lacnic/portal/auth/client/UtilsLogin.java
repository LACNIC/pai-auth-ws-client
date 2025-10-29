package net.lacnic.portal.auth.client;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_INVALID_CREDENTIALS;
import static net.lacnic.portal.auth.client.LogMessages.ERROR_OCCURRED;

import java.security.MessageDigest;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsLogin {

	private static final Logger logger = LoggerFactory.getLogger(UtilsLogin.class);

	private UtilsLogin() {
		// Utility class
	}

	public static LoginData login(String username, String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes());
			byte[] bytes = md.digest();
			String hash = new String(Base64.getEncoder().encode(bytes));
			password = "{SHA256}" + hash;
			return PortalWSClient.getLoginData(username, password);
		} catch (Exception e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);
			return new LoginData(ERROR_INVALID_CREDENTIALS);
		}
	}

	public static LoginData loginTfa(String username, String password, String totp) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes());
			byte[] bytes = md.digest();
			String hash = new String(Base64.getEncoder().encode(bytes));
			password = "{SHA256}" + hash;
			return PortalWSClient.getLoginDataTfa(username, password, totp);
		} catch (Exception e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);
			return new LoginData(ERROR_INVALID_CREDENTIALS);
		}
	}

}
