package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PortalAiClientTest {

	private Properties portalProperties(String baseUrl) {
		Properties props = new Properties();
		props.setProperty("URL_PORTAL_WS", baseUrl);
		return props;
	}

	private CloseableHttpClient mockHttp(MockedStatic<PortalHttpClient> httpMock, int status, String json) throws Exception {
		CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
		httpMock.when(PortalHttpClient::createInsecureClient).thenReturn(httpClient);
		CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
		Mockito.when(response.getEntity()).thenReturn(new StringEntity(json, ContentType.APPLICATION_JSON));
		Mockito.when(response.getCode()).thenReturn(status);
		Mockito.when(httpClient.execute(Mockito.any())).thenReturn(response);
		return httpClient;
	}

	@Test
	void resolveSendsBearerAndParsesCatalogBinding() throws Exception {
		String json = "{\"use\":\"DEFAULT\",\"provider\":\"OPENAI\",\"modelId\":\"gpt-5.5\",\"modelLabel\":\"GPT-5.5\",\"enabled\":true,\"timeoutSeconds\":60,\"credentialName\":\"OpenAI DEFAULT placeholder\"}";
		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/portal-ws"));
			CloseableHttpClient httpClient = mockHttp(httpMock, 200, json);

			AiResolveData result = PortalAiClient.resolve("raw-token", "DEFAULT");

			assertTrue(result.isSuccess());
			assertEquals("DEFAULT", result.getUse());
			assertEquals("OPENAI", result.getProvider());
			assertEquals("gpt-5.5", result.getModelId());
			assertEquals("GPT-5.5", result.getModelLabel());
			assertEquals(60, result.getTimeoutSeconds());

			ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
			Mockito.verify(httpClient).execute(captor.capture());
			assertEquals("https://example.com/portal-ws/ai/resolve/DEFAULT", captor.getValue().getUri().toString());
			assertEquals("Bearer raw-token", captor.getValue().getFirstHeader("Authorization").getValue());
		}
	}

	@Test
	void resolveMapsGatewayErrorBody() throws Exception {
		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/portal-ws"));
			mockHttp(httpMock, 403, "{\"error\":\"forbidden\"}");

			AiResolveData result = PortalAiClient.resolve("Bearer tok", "MiLACNIC_Query");

			assertFalse(result.isSuccess());
			assertEquals(403, result.getHttpStatus());
			assertEquals("forbidden", result.getError());
		}
	}

	@Test
	void chatPostsMessagesAndParsesText() throws Exception {
		String json = "{\"text\":\"hola\",\"use\":\"MiLACNIC_Query\",\"provider\":\"OPENAI\",\"modelId\":\"gpt-4o\",\"latencyMs\":12}";
		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/portal-ws/"));
			CloseableHttpClient httpClient = mockHttp(httpMock, 200, json);

			AiChatData result = PortalAiClient.chat("tok", "MiLACNIC_Query", List.of(AiChatMessage.system("sys"), AiChatMessage.user("ping")));

			assertTrue(result.isSuccess());
			assertEquals("hola", result.getText());
			assertEquals("MiLACNIC_Query", result.getUse());
			assertEquals(12L, result.getLatencyMs());

			ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
			Mockito.verify(httpClient).execute(captor.capture());
			HttpPost request = captor.getValue();
			assertEquals("https://example.com/portal-ws/ai/chat", request.getUri().toString());
			assertEquals("Bearer tok", request.getFirstHeader("Authorization").getValue());
			String body = EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8);
			assertTrue(body.contains("\"use\":\"MiLACNIC_Query\""));
			assertTrue(body.contains("\"role\":\"system\""));
			assertTrue(body.contains("\"content\":\"ping\""));
		}
	}

	@Test
	void chatRejectsBlankUseWithoutHttp() {
		AiChatData result = PortalAiClient.chat("tok", "  ", "hola");
		assertFalse(result.isSuccess());
		assertEquals("invalid_use", result.getError());
		assertEquals(400, result.getHttpStatus());
	}

	@Test
	void portalWsClientDelegatesToAiClient() throws Exception {
		String json = "{\"text\":\"ok\",\"use\":\"DEFAULT\",\"provider\":\"OPENAI\",\"modelId\":\"gpt-5.5\",\"latencyMs\":1}";
		try (MockedStatic<PortalWSClient> wsMock = Mockito.mockStatic(PortalWSClient.class, Mockito.CALLS_REAL_METHODS); MockedStatic<PortalHttpClient> httpMock = Mockito.mockStatic(PortalHttpClient.class)) {
			wsMock.when(PortalWSClient::getPaiProperties).thenReturn(portalProperties("https://example.com/portal-ws"));
			mockHttp(httpMock, 200, json);
			AiChatData result = PortalWSClient.chatAi("tok", "DEFAULT", "hola");
			assertTrue(result.isSuccess());
			assertEquals("ok", result.getText());
		}
	}
}
