package net.lacnic.portal.auth.client;

import java.io.Serializable;

public class AiChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private String role;
	private String content;

	public AiChatMessage() {
	}

	public AiChatMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}

	public static AiChatMessage user(String content) {
		return new AiChatMessage("user", content);
	}

	public static AiChatMessage system(String content) {
		return new AiChatMessage("system", content);
	}

	public static AiChatMessage assistant(String content) {
		return new AiChatMessage("assistant", content);
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
