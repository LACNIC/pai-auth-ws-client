package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class MySSLSocketFactoryTest {

	private MySSLSocketFactory mySSLSocketFactory;
	private KeyStore mockKeyStore;

	@BeforeEach
	void setUp() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {
		MockitoAnnotations.openMocks(this);
		mockKeyStore = mock(KeyStore.class);
		mySSLSocketFactory = new MySSLSocketFactory(mockKeyStore);
	}

	@Test
	void testSSLContextInitialization() throws NoSuchAlgorithmException {
		SSLContext sslContext = mySSLSocketFactory.sslContext;
		assertNotNull(sslContext, "SSLContext should not be null.");
		assertEquals("TLSv1.2", sslContext.getProtocol(), "SSLContext protocol should be TLSv1.2.");
	}

	@Test
	void testCreateSocket_withSocketHostAndPort() throws IOException {
		// Mock the base socket
		Socket baseSocket = mock(Socket.class);
		when(baseSocket.isConnected()).thenReturn(true); // Simulate that the socket is connected
		String host = "localhost";
		int port = 443;

		// Test the SSL socket creation
		Socket sslSocket = mySSLSocketFactory.createSocket(baseSocket, host, port, true);

		assertNotNull(sslSocket);
		assertTrue(sslSocket instanceof SSLSocket, "The created socket should be an instance of SSLSocket.");
	}

	@Test
	void testCreateSocket_withoutArguments() throws IOException {
		Socket sslSocket = mySSLSocketFactory.createSocket();

		assertNotNull(sslSocket);
		assertTrue(sslSocket instanceof SSLSocket, "The created socket should be an instance of SSLSocket.");
	}
}