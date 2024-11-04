package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	void testConstructorWithRoles_nonEmptyRoles() {
		List<String> roles = Arrays.asList("ADMIN", "USER");

		LoginData loginData = new LoginData(roles);

		assertTrue(loginData.isAuthenticated(), "Authenticated should be true when roles are non-empty");
		assertEquals(roles, loginData.getRoles(), "Roles should match the input list");
	}

	@Test
	void testConstructorWithRoles_emptyRoles() {
		List<String> roles = Collections.emptyList();

		LoginData loginData = new LoginData(roles);

		assertFalse(loginData.isAuthenticated(), "Authenticated should be false when roles are empty");
		assertEquals(roles, loginData.getRoles(), "Roles should match the input list");
	}

	@Test
	void testConstructorWithRolesAndUsername_nonEmptyRoles() {
		List<String> roles = Arrays.asList("ADMIN", "USER");
		String username = "testUser";

		LoginData loginData = new LoginData(roles, username);

		assertTrue(loginData.isAuthenticated(), "Authenticated should be true when roles are non-empty");
		assertEquals(roles, loginData.getRoles(), "Roles should match the input list");
		assertEquals(username, loginData.getUsername(), "Username should match the input username");
		assertEquals("", loginData.getError(), "Error should be empty when authenticated is true");
	}

	@Test
	void testConstructorWithRolesAndUsername_emptyRoles() {
		List<String> roles = Collections.emptyList();
		String username = "testUser";

		LoginData loginData = new LoginData(roles, username);

		assertFalse(loginData.isAuthenticated(), "Authenticated should be false when roles are empty");
		assertEquals(roles, loginData.getRoles(), "Roles should match the input list");
		assertEquals(username, loginData.getUsername(), "Username should match the input username");
		assertEquals("Error: verifique usuario y/o contraseña", loginData.getError(), "Error should be set when no roles are provided");
	}

	@Test
	void testErrorConstructor() {
		String error = "Invalid login";
		loginData = new LoginData(error);
		assertFalse(loginData.getAuthenticated(), "Authenticated should be false when error is set");
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

	@Test
	void testToString() {
		List<String> roles = new ArrayList<>();
		roles.add("USER");
		loginData.setRoles(roles);
		loginData.setAuthenticated(true);
		loginData.setUsername("Username");

		assertTrue(loginData.toString().contains("USER"));
		assertTrue(loginData.toString().contains("Username"));
	}
}