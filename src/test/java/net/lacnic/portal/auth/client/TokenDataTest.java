package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TokenDataTest {

	private TokenData tokenData;

	@BeforeEach
	void setUp() {
		tokenData = new TokenData();
	}

	@Test
	void testDefaultConstructor() {
		assertFalse(tokenData.getAuthenticated(), "Authenticated should be false by default");
		assertEquals("", tokenData.getToken(), "Token should be an empty string by default");
		assertNotNull(tokenData.getRoles(), "Roles list should be initialized");
		assertTrue(tokenData.getRoles().isEmpty(), "Roles list should be empty by default");
		assertEquals("", tokenData.getIpAllowed(), "IP allowed should be an empty string by default");
		assertEquals("", tokenData.getError(), "Error should be empty by default");
	}

	@Test
	void testConstructorWithError() {
		String errorMessage = "Test error";
		tokenData = new TokenData(errorMessage);

		assertFalse(tokenData.getAuthenticated(), "Authenticated should be false");
		assertEquals("", tokenData.getToken(), "Token should be an empty string");
		assertNotNull(tokenData.getRoles(), "Roles list should be initialized");
		assertTrue(tokenData.getRoles().isEmpty(), "Roles list should be empty");
		assertEquals("", tokenData.getIpAllowed(), "IP allowed should be an empty string");
		assertEquals(errorMessage, tokenData.getError(), "Error should match the provided message");
	}

	@Test
	void testConstructorWithRolesTokenIpAllowed() {
		List<String> roles = new ArrayList<>();
		roles.add("USER");
		String token = "sampleToken";
		String ipAllowed = "127.0.0.1";

		tokenData = new TokenData(roles, token, ipAllowed);

		assertTrue(tokenData.getAuthenticated(), "Authenticated should be true if roles are provided");
		assertEquals(roles, tokenData.getRoles(), "Roles should match the provided list");
		assertEquals(token, tokenData.getToken(), "Token should match the provided token");
		assertEquals(ipAllowed, tokenData.getIpAllowed(), "IP allowed should match the provided IP");
		assertEquals("", tokenData.getError(), "Error should be empty if authenticated");
	}

	@Test
	void testConstructorWithEmptyRoles() {
		List<String> roles = new ArrayList<>();
		String token = "sampleToken";
		String ipAllowed = "127.0.0.1";

		tokenData = new TokenData(roles, token, ipAllowed);

		assertFalse(tokenData.getAuthenticated(), "Authenticated should be false if roles are empty");
		assertEquals("No existen roles asociados a este token", tokenData.getError(), "Error message should indicate no roles are associated");
	}

	@Test
	void testSetAuthenticated() {
		tokenData.setAuthenticated(true);
		assertTrue(tokenData.getAuthenticated(), "Authenticated should be true after setting it to true");
	}

	@Test
	void testSetRoles() {
		List<String> roles = new ArrayList<>();
		roles.add("ADMIN");
		tokenData.setRoles(roles);

		assertEquals(roles, tokenData.getRoles(), "Roles should match the provided list");
	}

	@Test
	void testSetError() {
		String errorMessage = "Custom error";
		tokenData.setError(errorMessage);

		assertEquals(errorMessage, tokenData.getError(), "Error should match the provided message");
	}

	@Test
	void testSetToken() {
		String token = "newToken";
		tokenData.setToken(token);

		assertEquals(token, tokenData.getToken(), "Token should match the provided token");
	}

	@Test
	void testSetIpAllowed() {
		String ipAllowed = "192.168.1.1";
		tokenData.setIpAllowed(ipAllowed);

		assertEquals(ipAllowed, tokenData.getIpAllowed(), "IP allowed should match the provided IP");
	}

	@Test
	void testToString() {
		List<String> roles = new ArrayList<>();
		roles.add("USER");
		tokenData.setRoles(roles);
		tokenData.setAuthenticated(true);
		tokenData.setToken("sampleToken");
		tokenData.setError("No error");
		tokenData.setIpAllowed("127.0.0.1");

		assertTrue(tokenData.toString().contains("USER"));
		assertTrue(tokenData.toString().contains("sampleToken"));
		assertTrue(tokenData.toString().contains("No error"));
		assertTrue(tokenData.toString().contains("127.0.0.1"));
	}
}