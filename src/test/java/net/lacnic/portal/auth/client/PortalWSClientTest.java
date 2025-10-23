package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		String jsonRefreshed = "{\"authenticated\":true,\"token\":\"fresh-token\",\"roles\":[\"ADMIN\",\"USER\"],\"error\":\"\",\"ipAllowed\":\"127.0.0.1\"}";
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

			CloseableHttpResponse responseRefreshed = Mockito.mock(CloseableHttpResponse.class);
			HttpEntity entityRefreshed = Mockito.mock(HttpEntity.class);
			Mockito.when(entityRefreshed.getContent()).thenAnswer(invocation -> new ByteArrayInputStream(jsonRefreshed.getBytes(StandardCharsets.UTF_8)));
			Mockito.when(responseRefreshed.getEntity()).thenReturn(entityRefreshed);

			Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(responseInitial).thenReturn(responseRefreshed);

			TokenData initial = PortalWSClient.getTokenData(token);
			assertTrue(initial.isAuthenticated());
			assertEquals("cached-token", initial.getToken());

			Object cacheEntry = getCache().get(token);
			Field timestampField = cacheEntry.getClass().getDeclaredField("timestamp");
			timestampField.setAccessible(true);
			timestampField.setLong(cacheEntry, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));

			TokenData refreshed = PortalWSClient.getTokenData(token);

			assertNotNull(refreshed);
			assertEquals("fresh-token", refreshed.getToken());

			Object refreshedEntry = getCache().get(token);
			Field tokenDataField = refreshedEntry.getClass().getDeclaredField("tokenData");
			tokenDataField.setAccessible(true);
			TokenData cachedTokenData = (TokenData) tokenDataField.get(refreshedEntry);

			assertSame(cachedTokenData, refreshed);
			Mockito.verify(httpClient, Mockito.times(2)).execute(Mockito.any(HttpGet.class));
		}
	}
}
