package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TokenDataTest {
	private TokenData tokenData;

	@BeforeEach
	void setUp() {
		tokenData = new TokenData();
	}

	@Test
	void defaultConstructor_initializesFieldsCorrectly() {
		assertFalse(tokenData.isAuthenticated());
		assertTrue(tokenData.getToken().isEmpty());
		assertNotNull(tokenData.getRoles());
		assertTrue(tokenData.getRoles().isEmpty());
		assertTrue(tokenData.getError().isEmpty());
		assertTrue(tokenData.getIpAllowed().isEmpty());
	}

	@Test
	void constructorWithError_initializesFieldsCorrectly() {
		String errorMessage = "Error occurred";
		tokenData = new TokenData(errorMessage);

		assertFalse(tokenData.isAuthenticated());
		assertTrue(tokenData.getToken().isEmpty());
		assertNotNull(tokenData.getRoles());
		assertTrue(tokenData.getRoles().isEmpty());
		assertEquals(errorMessage, tokenData.getError());
		assertTrue(tokenData.getIpAllowed().isEmpty());
	}

	@Test
	void setAuthenticated_setsAuthenticatedFieldCorrectly() {
		tokenData.setAuthenticated(true);
		assertTrue(tokenData.isAuthenticated());

		tokenData.setAuthenticated(false);
		assertFalse(tokenData.isAuthenticated());
	}

	@Test
	void setToken_setsTokenFieldCorrectly() {
		String token = "testToken";
		tokenData.setToken(token);
		assertEquals(token, tokenData.getToken());
	}

	@Test
	void setRoles_setsRolesFieldCorrectly() {
		tokenData.setRoles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
		assertNotNull(tokenData.getRoles());
		assertEquals(2, tokenData.getRoles().size());
		assertTrue(tokenData.getRoles().contains("ROLE_USER"));
		assertTrue(tokenData.getRoles().contains("ROLE_ADMIN"));
	}

	@Test
	void setError_setsErrorFieldCorrectly() {
		String error = "Some error";
		tokenData.setError(error);
		assertEquals(error, tokenData.getError());
	}

	@Test
	void setIpAllowed_setsIpAllowedFieldCorrectly() {
		String ip = "192.168.0.1";
		tokenData.setIpAllowed(ip);
		assertEquals(ip, tokenData.getIpAllowed());
	}
}
