package net.lacnic.portal.auth.client;

import static net.lacnic.portal.auth.client.LogMessages.ERROR_OCCURRED;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class PortalAiClient {

	private static final Logger logger = LoggerFactory.getLogger(PortalAiClient.class);
	private static final String AUTHORIZATION = "Authorization";
	private static final String URL_PORTAL_WS = "URL_PORTAL_WS";
	private static final String DEFAULT_PORTAL_WS = "https://pai-test.dev.lacnic.net/portal-ws";
	private static final String CLIENT_ERROR = "Error en el cliente Java de gateway IA";

	private PortalAiClient() {
	}

	public static AiResolveData resolve(String token, String use) {
		if (use == null || use.isBlank()) {
			return failureResolve("invalid_use", 400);
		}
		try {
			HttpResponseBody response = execute(new HttpGet(baseUrl() + "/ai/resolve/" + use.trim()), token, null);
			if (response.status >= 200 && response.status < 300) {
				AiResolveData data = mapper().readValue(response.body, AiResolveData.class);
				data.setSuccess(true);
				data.setHttpStatus(response.status);
				data.setError("");
				return data;
			}
			return failureResolve(readError(response.body), response.status);
		} catch (Exception ex) {
			logger.error(ERROR_OCCURRED, ex.getMessage(), ex);
			return new AiResolveData(CLIENT_ERROR);
		}
	}

	public static AiChatData chat(String token, String use, String userMessage) {
		List<AiChatMessage> messages = new ArrayList<>();
		if (userMessage != null) {
			messages.add(AiChatMessage.user(userMessage));
		}
		return chat(token, use, messages);
	}

	public static AiChatData chat(String token, String use, List<AiChatMessage> messages) {
		if (use == null || use.isBlank()) {
			return failureChat("invalid_use", 400);
		}
		try {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("use", use.trim());
			body.put("messages", messages == null ? List.of() : messages);
			HttpPost request = new HttpPost(baseUrl() + "/ai/chat");
			HttpResponseBody response = execute(request, token, mapper().writeValueAsString(body));
			if (response.status >= 200 && response.status < 300) {
				AiChatData data = mapper().readValue(response.body, AiChatData.class);
				data.setSuccess(true);
				data.setHttpStatus(response.status);
				data.setError("");
				return data;
			}
			return failureChat(readError(response.body), response.status);
		} catch (Exception ex) {
			logger.error(ERROR_OCCURRED, ex.getMessage(), ex);
			return new AiChatData(CLIENT_ERROR);
		}
	}

	private static HttpResponseBody execute(HttpUriRequestBase request, String token, String jsonBody) throws Exception {
		request.setHeader(AUTHORIZATION, bearer(token));
		request.setHeader("Accept", "application/json");
		if (jsonBody != null) {
			request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
		}
		try (CloseableHttpClient client = PortalHttpClient.createInsecureClient(); CloseableHttpResponse response = client.execute(request)) {
			String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
			return new HttpResponseBody(response.getCode(), body);
		}
	}

	private static String bearer(String token) {
		if (token == null || token.isBlank()) {
			return "";
		}
		return token.regionMatches(true, 0, "Bearer ", 0, 7) ? token : "Bearer " + token;
	}

	private static String baseUrl() {
		Properties props = PortalWSClient.getPaiProperties();
		String url = props.getProperty(URL_PORTAL_WS, DEFAULT_PORTAL_WS);
		return url == null || url.isBlank() ? DEFAULT_PORTAL_WS : url.replaceAll("/+$", "");
	}

	private static String readError(String body) {
		if (body == null || body.isBlank()) {
			return "service_unavailable";
		}
		try {
			JsonNode node = mapper().readTree(body);
			if (node.hasNonNull("error")) {
				return node.get("error").asText();
			}
		} catch (IOException ignored) {
			return body;
		}
		return body;
	}

	private static AiResolveData failureResolve(String error, int status) {
		AiResolveData data = new AiResolveData(error);
		data.setHttpStatus(status);
		return data;
	}

	private static AiChatData failureChat(String error, int status) {
		AiChatData data = new AiChatData(error);
		data.setHttpStatus(status);
		return data;
	}

	private static ObjectMapper mapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return objectMapper;
	}

	private static final class HttpResponseBody {
		private final int status;
		private final String body;

		private HttpResponseBody(int status, String body) {
			this.status = status;
			this.body = body;
		}
	}
}
