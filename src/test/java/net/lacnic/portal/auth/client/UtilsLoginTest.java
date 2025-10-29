package net.lacnic.portal.auth.client;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_INVALID_CREDENTIALS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.MessageDigest;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class UtilsLoginTest {

	@BeforeEach
	void setUp() {
	}

	@Test
	void testLogin_successfulLogin() throws Exception {
		String username = "testUser";
		String password = "testPassword";
		String encodedPassword = "{SHA256}" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(password.getBytes()));

		LoginData mockLoginData = new LoginData();
		try (MockedStatic<PortalWSClient> mockedPortalWSClient = mockStatic(PortalWSClient.class); MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {

			MessageDigest mockDigest = mock(MessageDigest.class);
			when(mockDigest.digest()).thenReturn(Base64.getDecoder().decode(encodedPassword.substring(8)));
			mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenReturn(mockDigest);
			mockedPortalWSClient.when(() -> PortalWSClient.getLoginData(username, encodedPassword)).thenReturn(mockLoginData);

			LoginData result = UtilsLogin.login(username, password);

			assertNotNull(result);
			assertEquals(mockLoginData, result);
		}
	}

	@Test
	void testLogin_exceptionInDigest() {
		String username = "testUser";
		String password = "testPassword";

		try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
			mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(new RuntimeException("Digest error"));

			LoginData result = UtilsLogin.login(username, password);

			assertNotNull(result);
			assertFalse(result.isAuthenticated());
			assertEquals(ERROR_INVALID_CREDENTIALS, result.getError());
		}
	}

	@Test
	void testLoginTfa_successfulLogin() throws Exception {
		String username = "user";
		String password = "password";
		String totp = "654321";
		String encodedPassword = "{SHA256}" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(password.getBytes()));

		LoginData expectedData = new LoginData();

		try (MockedStatic<PortalWSClient> mockedPortalWSClient = mockStatic(PortalWSClient.class)) {
			mockedPortalWSClient.when(() -> PortalWSClient.getLoginDataTfa(username, encodedPassword, totp)).thenReturn(expectedData);

			LoginData result = UtilsLogin.loginTfa(username, password, totp);

			assertNotNull(result);
			assertEquals(expectedData, result);
		}
	}

	@Test
	void testLoginTfa_returnsErrorWhenPortalThrows() {
		String username = "user";
		String password = "password";
		String totp = "654321";

		try (MockedStatic<PortalWSClient> mockedPortalWSClient = mockStatic(PortalWSClient.class)) {
			mockedPortalWSClient.when(() -> PortalWSClient.getLoginDataTfa(Mockito.eq(username), Mockito.anyString(), Mockito.eq(totp))).thenThrow(new RuntimeException("WS error"));

			LoginData result = UtilsLogin.loginTfa(username, password, totp);

			assertNotNull(result);
			assertFalse(result.isAuthenticated());
			assertEquals(ERROR_INVALID_CREDENTIALS, result.getError());
		}
	}

	@Test
	void testLogin_returnsErrorWhenPortalThrows() {
		String username = "user";
		String password = "password";

		try (MockedStatic<PortalWSClient> mockedPortalWSClient = mockStatic(PortalWSClient.class)) {
			mockedPortalWSClient.when(() -> PortalWSClient.getLoginData(Mockito.eq(username), Mockito.anyString())).thenThrow(new RuntimeException("WS error"));

			LoginData result = UtilsLogin.login(username, password);

			assertNotNull(result);
			assertFalse(result.isAuthenticated());
			assertEquals(ERROR_INVALID_CREDENTIALS, result.getError());
		}
	}
}
