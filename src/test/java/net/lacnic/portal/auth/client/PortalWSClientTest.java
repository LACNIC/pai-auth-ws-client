package net.lacnic.portal.auth.client;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_INVALID_CREDENTIALS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@SuppressWarnings("deprecation")
class PortalWSClientTest {

	@BeforeEach
	void clearCache() throws Exception {
		getCache().clear();
	}

	@SuppressWarnings("unchecked")
	private ConcurrentHashMap<String, Object> getCache() throws Exception {
		Field cacheField = PortalWSClient.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		return (ConcurrentHashMap<String, Object>) cacheField.get(null);
	}

	private Properties portalProperties(String baseUrl) {
		Properties props = new Properties();
		props.setProperty("PORTAL_APIKEY", "api-key");
		if (baseUrl != null) {
			props.setProperty("URL_PORTAL_WS", baseUrl);
		}
		return props;
	}

	private CloseableHttpClient mockHttpClientReturning(MockedStatic<PortalHttpClient> httpMock, String json) throws Exception {
		CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
		httpMock.when(PortalHttpClient::createInsecureClient).thenReturn(httpClient);

		CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
		HttpEntity entity = Mockito.mock(HttpEntity.class);
		Mockito.when(entity.getContent()).thenAnswer(invocation -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
		Mockito.when(response.getEntity()).thenReturn(entity);
		Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);
		return httpClient;
	}

	@Test
	void testGetLoginDataParsesResponseAndSendsLoginHeaders() throws Exception {
		String username = "user@example.com";
		String password = "{SHA256}hash";
		String json = "{\"authenticated\":true,\"username\":\"user@example.com\",\"roles\":[\"ADMIN\",\"USER\"],\"error\":\"\"}";

		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/api"));
			CloseableHttpClient httpClient = mockHttpClientReturning(httpMock, json);

			LoginData result = PortalWSClient.getLoginData(username, password);

			assertTrue(result.isAuthenticated());
			assertEquals(username, result.getUsername());
			assertEquals(2, result.getRoles().size());
			assertEquals("ADMIN", result.getRoles().get(0));

			ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
			Mockito.verify(httpClient).execute(requestCaptor.capture());
			HttpGet request = requestCaptor.getValue();
			assertEquals("https://example.com/api/login", request.getUri().toString());
			assertEquals("api-key", request.getFirstHeader("PORTAL_APIKEY").getValue());
			assertEquals(username, request.getFirstHeader("PORTAL_USUARIO").getValue());
			assertEquals(password, request.getFirstHeader("PORTAL_PASS").getValue());
			assertEquals("", request.getFirstHeader("PORTAL_TOTP").getValue());
		}
	}

	@Test
	void testGetLoginDataUsesDefaultPortalUrlWhenPropertyIsMissing() throws Exception {
		String json = "{\"authenticated\":true,\"username\":\"user\",\"roles\":[\"USER\"],\"error\":\"\"}";

		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties(null));
			CloseableHttpClient httpClient = mockHttpClientReturning(httpMock, json);

			PortalWSClient.getLoginData("user", "password");

			ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
			Mockito.verify(httpClient).execute(requestCaptor.capture());
			assertEquals("https://pai-test.dev.lacnic.net/portal-ws/login", requestCaptor.getValue().getUri().toString());
		}
	}

	@Test
	void testGetLoginDataReturnsInvalidCredentialsWhenJsonCannotBeParsed() throws Exception {
		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/api"));
			mockHttpClientReturning(httpMock, "invalid-json");

			LoginData result = PortalWSClient.getLoginData("user", "password");

			assertFalse(result.isAuthenticated());
			assertEquals(ERROR_INVALID_CREDENTIALS, result.getError());
		}
	}

	@Test
	void testGetLoginDataTfaParsesResponseAndSendsTotpHeader() throws Exception {
		String username = "user@example.com";
		String password = "{SHA256}hash";
		String totp = "654321";
		String json = "{\"authenticated\":true,\"username\":\"user@example.com\",\"roles\":[\"MFA\"],\"error\":\"\"}";

		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/api"));
			CloseableHttpClient httpClient = mockHttpClientReturning(httpMock, json);

			LoginData result = PortalWSClient.getLoginDataTfa(username, password, totp);

			assertTrue(result.isAuthenticated());
			assertEquals(username, result.getUsername());
			assertEquals("MFA", result.getRoles().get(0));

			ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
			Mockito.verify(httpClient).execute(requestCaptor.capture());
			HttpGet request = requestCaptor.getValue();
			assertEquals("https://example.com/api/login-tfa", request.getUri().toString());
			assertEquals("api-key", request.getFirstHeader("PORTAL_APIKEY").getValue());
			assertEquals(username, request.getFirstHeader("PORTAL_USUARIO").getValue());
			assertEquals(password, request.getFirstHeader("PORTAL_PASS").getValue());
			assertEquals(totp, request.getFirstHeader("PORTAL_TOTP").getValue());
		}
	}

	@Test
	void testGetTokenDataSendsBearerAuthorizationHeader() throws Exception {
		String json = "{\"authenticated\":true,\"token\":\"abc123\",\"roles\":[\"USER\"],\"error\":\"\",\"ipAllowed\":\"127.0.0.1\"}";

		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/api"));
			CloseableHttpClient httpClient = mockHttpClientReturning(httpMock, json);

			PortalWSClient.getTokenData("raw-token");
			PortalWSClient.getTokenData("Bearer existing-token");

			ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
			Mockito.verify(httpClient, Mockito.times(2)).execute(requestCaptor.capture());

			HttpGet firstRequest = requestCaptor.getAllValues().get(0);
			assertEquals("https://example.com/api/authorization", firstRequest.getUri().toString());
			assertEquals("Bearer raw-token", firstRequest.getFirstHeader("Authorization").getValue());

			HttpGet secondRequest = requestCaptor.getAllValues().get(1);
			assertEquals("https://example.com/api/authorization", secondRequest.getUri().toString());
			assertEquals("Bearer existing-token", secondRequest.getFirstHeader("Authorization").getValue());
		}
	}

	@Test
	void testGetTokenDataCachesResponse() throws Exception {
		String token = "token-123";
		String json = "{\"authenticated\":true,\"token\":\"abc123\",\"roles\":[\"USER\"],\"error\":\"\",\"ipAllowed\":\"127.0.0.1\"}";
		Properties props = new Properties();
		props.setProperty("PORTAL_APIKEY", "api-key");
		props.setProperty("URL_PORTAL_WS", "https://example.com");

		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(props);

			CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
			httpMock.when(PortalHttpClient::createInsecureClient).thenReturn(httpClient);

			CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
			HttpEntity entity = Mockito.mock(HttpEntity.class);
			Mockito.when(entity.getContent()).thenAnswer(invocation -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
			Mockito.when(response.getEntity()).thenReturn(entity);
			Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);

			TokenData firstCall = PortalWSClient.getTokenData(token);

			assertNotNull(firstCall);
			assertTrue(firstCall.isAuthenticated());
			assertEquals("abc123", firstCall.getToken());
			assertEquals(1, firstCall.getRoles().size());
			assertEquals("USER", firstCall.getRoles().get(0));

			TokenData secondCall = PortalWSClient.getTokenData(token);

			assertSame(firstCall, secondCall);
			Mockito.verify(httpClient, Mockito.times(1)).execute(Mockito.any(HttpGet.class));
		}
	}

	@Test
	void testGetTokenDataReturnsCachedDataWhenRefreshFails() throws Exception {
		String token = "token-456";
		String jsonInitial = "{\"authenticated\":true,\"token\":\"cached-token\",\"roles\":[\"ADMIN\"],\"error\":\"\",\"ipAllowed\":\"127.0.0.1\"}";
		String invalidJson = "invalid";
		Properties props = new Properties();
		props.setProperty("PORTAL_APIKEY", "api-key");
		props.setProperty("URL_PORTAL_WS", "https://example.com");

		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(props);

			CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
			httpMock.when(PortalHttpClient::createInsecureClient).thenReturn(httpClient);

			CloseableHttpResponse responseInitial = Mockito.mock(CloseableHttpResponse.class);
			HttpEntity entityInitial = Mockito.mock(HttpEntity.class);
			Mockito.when(entityInitial.getContent()).thenAnswer(invocation -> new ByteArrayInputStream(jsonInitial.getBytes(StandardCharsets.UTF_8)));
			Mockito.when(responseInitial.getEntity()).thenReturn(entityInitial);

			CloseableHttpResponse responseInvalid = Mockito.mock(CloseableHttpResponse.class);
			HttpEntity entityInvalid = Mockito.mock(HttpEntity.class);
			Mockito.when(entityInvalid.getContent()).thenAnswer(invocation -> new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8)));
			Mockito.when(responseInvalid.getEntity()).thenReturn(entityInvalid);

			Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(responseInitial).thenReturn(responseInvalid);

			TokenData initial = PortalWSClient.getTokenData(token);
			assertTrue(initial.isAuthenticated());
			assertEquals("cached-token", initial.getToken());

			Object cacheEntry = getCache().get(token);
			Field timestampField = cacheEntry.getClass().getDeclaredField("timestamp");
			timestampField.setAccessible(true);
			timestampField.setLong(cacheEntry, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));

			TokenData refreshed = PortalWSClient.getTokenData(token);

			assertNotNull(refreshed);
			assertSame(initial, refreshed);
			Mockito.verify(httpClient, Mockito.times(2)).execute(Mockito.any(HttpGet.class));
		}
	}
}
