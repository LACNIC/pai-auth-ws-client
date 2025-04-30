package net.lacnic.portal.auth.client;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

public class PortalHttpClient {

	public static CloseableHttpClient createInsecureClient() throws Exception {
		// Confía en todos los certificados
		SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();

		// Crea el socket factory ignorando validación del hostname
		SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2", "TLSv1.3" }, null, NoopHostnameVerifier.INSTANCE);

		return HttpClients.custom().setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslSocketFactory).build()).build();
	}
}