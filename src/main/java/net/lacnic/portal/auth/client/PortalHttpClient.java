package net.lacnic.portal.auth.client;

import java.security.KeyStore;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class PortalHttpClient {
	private static final Logger logger = LoggerFactory.getLogger(PortalHttpClient.class);

	public static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			try (DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params)) {
				int timeout = 40; // seconds
				HttpParams httpParams = httpClient.getParams();
				httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
				httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);
				httpParams.setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, new Long(timeout * 1000));
				return httpClient;
			}
		} catch (Exception e) {
			logger.error("An error occurred: {}", e.getMessage(), e);
			return new DefaultHttpClient();
		}
	}

	public static HttpClient getCloseableHttpClient() {
		try {
			// Initialize truststore and custom socket factory (sf)
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// Create HttpParams and SchemeRegistry
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			// Initialize ClientConnectionManager
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			// Use try-with-resources to ensure httpClient is closed after use
			try (CloseableHttpClient httpClient = new DefaultHttpClient(ccm, params)) {
				int timeout = 40; // seconds
				HttpParams httpParams = httpClient.getParams();
				httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
				httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);
				httpParams.setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, (long) timeout * 1000);

				return httpClient;
			}
		} catch (Exception e) {
			logger.error("An error occurred: {}", e.getMessage(), e);
			return HttpClients.createDefault();
		}
	}
}
