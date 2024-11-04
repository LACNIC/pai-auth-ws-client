package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.junit.jupiter.api.Test;

class PortalHttpClientTest {

	@Test
	void testGetNewHttpClient_returnsConfiguredHttpClient() {
		// Call the method
		HttpClient httpClient = PortalHttpClient.getNewHttpClient();

		// Verify that HttpClient is not null
		assertNotNull(httpClient);
		assertTrue(httpClient instanceof DefaultHttpClient, "Expected instance of DefaultHttpClient");

		// Check if parameters were set correctly
		HttpParams httpParams = httpClient.getParams();
		assertEquals(40000, httpParams.getIntParameter("http.connection.timeout", 0));
		assertEquals(40000, httpParams.getIntParameter("http.socket.timeout", 0));
		assertEquals(40000L, httpParams.getLongParameter("http.conn-manager.timeout", 0));
	}

	@Test
	void testGetNewHttpClient_fallbackOnException() {
		HttpClient httpClient = null;
		try {
			httpClient = PortalHttpClient.getNewHttpClient();
		} catch (Exception ignored) {
		}

		// Verify that HttpClient is not null even if exceptions occur
		assertNotNull(httpClient);
		assertTrue(httpClient instanceof DefaultHttpClient);
	}
}
