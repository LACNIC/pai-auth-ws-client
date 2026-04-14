package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;

class PortalHttpClientTest {

	@Test
	void testCreateInsecureClientReturnsClient() throws IOException {
		try (CloseableHttpClient client = PortalHttpClient.createInsecureClient()) {
			assertNotNull(client);
		}
	}
}
