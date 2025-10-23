package net.lacnic.portal.auth.client;

import java.security.MessageDigest;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_OCCURRED;
import static net.lacnic.portal.auth.client.LogMessages.ERROR_INVALID_CREDENTIALS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsLogin {

	private static final Logger logger = LoggerFactory.getLogger(UtilsLogin.class);

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

	public static String getBearer(HttpServletRequest request) throws Exception {
		String token = request.getHeader("Authorization");
		if (token != null && !token.isEmpty()) {
			token = token.trim().replaceFirst("Bearer ", "").trim();
			return token;
		}
		throw new Exception("No cotiene el header Authorization, ejemplo Authorization: Bearer 123456789");
	}

	public static LoginData loginTfa(String username, String password, String totp) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes());
			byte[] bytes = md.digest();
			String hash = new String(Base64.getEncoder().encode(bytes));
			password = "{SHA256}" + hash;
			LoginData infoLDAP = PortalWSClient.getLoginDataTfa(username, password, totp);
			return infoLDAP;
		} catch (Exception e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);

            return new LoginData(ERROR_INVALID_CREDENTIALS);
		}
	}

	public static String getHeaderAuthorization(HttpServletRequest request) throws Exception {
		String token = request.getHeader("Authorization");
		if (token != null && !token.isEmpty()) {
			return token;
		}
		throw new Exception("No cotiene el header Authorization, ejemplo Authorization: Bearer 123456789");
	}

}
