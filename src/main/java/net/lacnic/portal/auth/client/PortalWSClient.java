package net.lacnic.portal.auth.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PortalWSClient {

	public static final String confDir = System.getProperty("jboss.server.config.dir");

	private static final String URL_PORTAL_WS = "URL_PORTAL_WS";
	private static final String PORTAL_AUTHORIZATION = "Authorization";
	private static final String PORTAL_APIKEY = "PORTAL_APIKEY";
	private static final String PORTAL_USUARIO = "PORTAL_USUARIO";
	private static final String PORTAL_PASS = "PORTAL_PASS";
	private static final String PORTAL_TOTP = "PORTAL_TOTP";

	public PortalWSClient() {

	}

	public static TokenData getTokenData(String token) {
		try {
			String jsonData = readUrlToken((getURLWS() + "/authorization"), token);
			ObjectMapper objectMapper = newMapper();

			TokenData tokenData = objectMapper.readValue(jsonData, new TypeReference<TokenData>() {
			});
			print(tokenData.toString());
			return tokenData;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new TokenData("Error en el cliente Java");
	}

	public static void print(String printeable) {
		System.out.println("*******************");
		System.out.println(printeable);
		System.out.println("*******************");
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
			e.printStackTrace();
		}
		return new LoginData("Error: verifique usuario y/o contraseña");
	}

	public static String readUrl(String urlString, String username, String password) {
		return readUrl(urlString, username, password, "");
	}

	private static String readUrl(String urlString, String username, String password, String totp) {
		try {
			HttpClient client = PortalHttpClient.getNewHttpClient();
			HttpGet request = new HttpGet(urlString);
			request.setHeader(PORTAL_APIKEY, getAuthToken());
			request.setHeader(PORTAL_USUARIO, username);
			request.setHeader(PORTAL_PASS, password);
			request.setHeader(PORTAL_TOTP, totp);

			HttpResponse response = client.execute(request);
			StringBuffer result = new StringBuffer();

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String readUrlToken(String urlString, String token) {
		try {
			if (!token.startsWith("Bearer")) {
				token = "Bearer " + token;
			}
			HttpClient client = PortalHttpClient.getNewHttpClient();
			HttpGet request = new HttpGet(urlString);
			request.setHeader(PORTAL_AUTHORIZATION, token);

			HttpResponse response = client.execute(request);
			StringBuffer result = new StringBuffer();

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
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
		try (InputStream input = new FileInputStream(confDir.concat("/pai.properties"))) {
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
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
			e.printStackTrace();
		}
		return new LoginData("Error ws nuevo");
	}
}
