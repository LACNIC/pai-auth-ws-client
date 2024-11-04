package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginDataTest {

	private LoginData loginData;

	@BeforeEach
	void setUp() {
		loginData = new LoginData();
	}

	@Test
	void defaultConstructor_initializesFieldsCorrectly() {
		assertFalse(loginData.isAuthenticated());
		assertEquals("", loginData.getUsername());
		assertNotNull(loginData.getRoles());
		assertTrue(loginData.getRoles().isEmpty());
		assertEquals("", loginData.getError());
	}

	@Test
	void constructorWithError_initializesFieldsCorrectly() {
		String errorMessage = "Authentication failed";
		loginData = new LoginData(errorMessage);

		assertFalse(loginData.isAuthenticated());
		assertEquals("", loginData.getUsername());
		assertNotNull(loginData.getRoles());
		assertTrue(loginData.getRoles().isEmpty());
		assertEquals(errorMessage, loginData.getError());
	}

	@Test
	void setAuthenticated_setsAuthenticatedFieldCorrectly() {
		loginData.setAuthenticated(true);
		assertTrue(loginData.isAuthenticated());

		loginData.setAuthenticated(false);
		assertFalse(loginData.isAuthenticated());
	}

	@Test
	void setUsername_setsUsernameFieldCorrectly() {
		String username = "testUser";
		loginData.setUsername(username);
		assertEquals(username, loginData.getUsername());
	}

	@Test
	void setRoles_setsRolesFieldCorrectly() {
		loginData.setRoles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
		assertNotNull(loginData.getRoles());
		assertEquals(2, loginData.getRoles().size());
		assertTrue(loginData.getRoles().contains("ROLE_USER"));
		assertTrue(loginData.getRoles().contains("ROLE_ADMIN"));
	}

	@Test
	void setError_setsErrorFieldCorrectly() {
		String error = "Some error";
		loginData.setError(error);
		assertEquals(error, loginData.getError());
	}
}