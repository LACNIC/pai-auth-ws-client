package net.lacnic.portal.auth.client;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_INVALID_CREDENTIALS;
import static net.lacnic.portal.auth.client.LogMessages.ERROR_OCCURRED;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("deprecation")
public class PortalWSClient {
	private static final Logger logger = LoggerFactory.getLogger(PortalWSClient.class);
	public static final String CONF_DIR = System.getProperty("jboss.server.config.dir");

	private static final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

	private static final String URL_PORTAL_WS = "URL_PORTAL_WS";
	private static final String PORTAL_AUTHORIZATION = "Authorization";
	private static final String PORTAL_APIKEY = "PORTAL_APIKEY";
	private static final String PORTAL_USUARIO = "PORTAL_USUARIO";
	private static final String PORTAL_PASS = "PORTAL_PASS";
	private static final String PORTAL_TOTP = "PORTAL_TOTP";
	private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(2); // 2 minutos

	public static TokenData getTokenData(String token) {
		CacheEntry cached = cache.get(token);
		try {
			if (cached != null) {
				if (!cached.isExpired()) {
					return cached.tokenData;
				} else {
					cache.remove(token);
				}
			}
			String jsonData = readUrlToken((getURLWS() + "/authorization"), token);
			ObjectMapper objectMapper = newMapper();
			TokenData tokenData = objectMapper.readValue(jsonData, new TypeReference<TokenData>() {
			});

			print(tokenData.toString());
			cache.put(token, new CacheEntry(tokenData));
			return tokenData;
		} catch (IOException e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);
			if (cached != null) {
				cached.extend();
				cache.put(token, cached); // Reponemos el cache extendido
				logger.warn("Error al obtener token. Usando cache extendido temporalmente.");
				return cached.tokenData;
			}
		}
		return new TokenData("Error en el cliente Java");
	}

	public static void print(String printeable) {
		if (logger.isDebugEnabled()) {
			logger.debug("*******************\n{}\n*******************", printeable);
		}
	}

	private static class CacheEntry {
		private final TokenData tokenData;
		private long timestamp;

		public CacheEntry(TokenData tokenData) {
			this.tokenData = tokenData;
			this.timestamp = System.currentTimeMillis();
		}

		public boolean isExpired() {
			return (System.currentTimeMillis() - timestamp) > CACHE_DURATION_MS;
		}

		public void extend() {
			this.timestamp = System.currentTimeMillis();
		}
	}

	public static LoginData getLoginData(String username, String password) {
		try {
			String jsonData = readUrl((getURLWS() + "/login"), username, password);
			ObjectMapper objectMapper = newMapper();

			LoginData dataLDAP = objectMapper.readValue(jsonData, new TypeReference<LoginData>() {
			});
			print(dataLDAP.toString());

			return dataLDAP;
		} catch (IOException e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);

		}
		return new LoginData(ERROR_INVALID_CREDENTIALS);
	}

	public static String readUrl(String urlString, String username, String password) {
		return readUrl(urlString, username, password, "");
	}

	private static String readUrl(String urlString, String username, String password, String totp) {
		try (CloseableHttpClient client = PortalHttpClient.createInsecureClient()) {
			HttpGet request = new HttpGet(urlString);
			request.setHeader(PORTAL_APIKEY, getAuthToken());
			request.setHeader(PORTAL_USUARIO, username);
			request.setHeader(PORTAL_PASS, password);
			request.setHeader(PORTAL_TOTP, totp);

			CloseableHttpResponse response = client.execute(request);
			StringBuilder result = new StringBuilder();

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			return result.toString();
		} catch (Exception e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);

			return null;
		}
	}

	private static String readUrlToken(String urlString, String token) {
		try (CloseableHttpClient client = PortalHttpClient.createInsecureClient()) {
			if (!token.startsWith("Bearer")) {
				token = "Bearer " + token;
			}
			HttpGet request = new HttpGet(urlString);
			request.setHeader(PORTAL_AUTHORIZATION, token);

			CloseableHttpResponse response = client.execute(request);
			StringBuilder result = new StringBuilder();

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			return result.toString();
		} catch (Exception e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);

			return null;
		}
	}

	private static String getAuthToken() {
		Properties props = getPaiProperties();
		return props.getProperty(PORTAL_APIKEY);
	}

	private static String getURLWS() {
		Properties props = getPaiProperties();
		return props.getProperty(URL_PORTAL_WS, "https://pai-test.dev.lacnic.net/portal-ws");

	}

	public static Properties getPaiProperties() {
		Properties prop = new Properties();
		try (InputStream input = new FileInputStream(CONF_DIR.concat("/pai.properties"))) {
			prop.load(input);
		} catch (IOException ex) {
			logger.error(ERROR_OCCURRED, ex.getMessage(), ex);

		}
		return prop;
	}

	private static ObjectMapper newMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return objectMapper;
	}

	public static LoginData getLoginDataTfa(String username, String password, String totp) {
		try {
			String jsonData = readUrl((getURLWS() + "/login-tfa"), username, password, totp);
			ObjectMapper objectMapper = newMapper();

			LoginData dataLDAP = objectMapper.readValue(jsonData, new TypeReference<LoginData>() {
			});
			print(dataLDAP.toString());

			return dataLDAP;
		} catch (IOException e) {
			logger.error(ERROR_OCCURRED, e.getMessage(), e);

		}
		return new LoginData("Error ws nuevo");
	}

}
