package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.MessageDigest;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class UtilsLoginTest {

	private HttpServletRequest mockRequest;

	@BeforeEach
	void setUp() {
		mockRequest = mock(HttpServletRequest.class);
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
			assertEquals("Error: verifique usuario y/o contraseña", result.getError());
		}
	}

	@Test
	void testGetBearer_validToken() throws Exception {
		when(mockRequest.getHeader("Authorization")).thenReturn("Bearer validToken");

		String token = UtilsLogin.getBearer(mockRequest);

		assertEquals("validToken", token);
	}

}