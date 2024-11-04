package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	void testDefaultConstructor() {
		assertFalse(loginData.isAuthenticated(), "Authenticated should be false by default");
		assertEquals("", loginData.getUsername(), "Username should be an empty string by default");
		assertNotNull(loginData.getRoles(), "Roles should be initialized");
		assertTrue(loginData.getRoles().isEmpty(), "Roles should be empty by default");
		assertEquals("", loginData.getError(), "Error should be an empty string by default");
	}

	@Test
	void testErrorConstructor() {
		String error = "Invalid login";
		loginData = new LoginData(error);
		assertFalse(loginData.isAuthenticated(), "Authenticated should be false when error is set");
		assertEquals("", loginData.getUsername(), "Username should be empty by default with error constructor");
		assertNotNull(loginData.getRoles(), "Roles should be initialized");
		assertTrue(loginData.getRoles().isEmpty(), "Roles should be empty by default with error constructor");
		assertEquals(error, loginData.getError(), "Error should be set to the provided error message");
	}

	@Test
	void testSetAuthenticated() {
		loginData.setAuthenticated(true);
		assertTrue(loginData.isAuthenticated(), "Authenticated should be true after setting it");
	}

	@Test
	void testSetUsername() {
		String username = "user1";
		loginData.setUsername(username);
		assertEquals(username, loginData.getUsername(), "Username should be set correctly");
	}

	@Test
	void testSetRoles() {
		loginData.setRoles(Arrays.asList("admin", "user"));
		assertEquals(2, loginData.getRoles().size(), "Roles list size should match the assigned list");
		assertTrue(loginData.getRoles().contains("admin"), "Roles should contain 'admin'");
		assertTrue(loginData.getRoles().contains("user"), "Roles should contain 'user'");
	}

	@Test
	void testSetRolesWithNull() {
		loginData.setRoles(null);
		assertNull(loginData.getRoles(), "Roles should be null if set to null explicitly");
	}

	@Test
	void testSetError() {
		String error = "Login failed";
		loginData.setError(error);
		assertEquals(error, loginData.getError(), "Error message should match the set value");
	}
}