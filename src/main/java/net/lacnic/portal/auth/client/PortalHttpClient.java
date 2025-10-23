package net.lacnic.portal.auth.client;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

public class PortalHttpClient {

	private PortalHttpClient() {
		// Utility class
	}

	public static CloseableHttpClient createInsecureClient() {
		try {
			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
			DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
			PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create().setTlsSocketStrategy(tlsStrategy).build();
			return HttpClients.custom().setConnectionManager(connManager).build();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Unable to create insecure HTTP client", e);
		}
	}
}
