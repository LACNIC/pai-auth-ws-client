package net.lacnic.portal.auth.client;

import java.io.Serializable;

public class AiRelevanceUsage implements Serializable {

	private static final long serialVersionUID = 1L;
	private long inputTokens;
	private long outputTokens;

	public long getInputTokens() {
		return inputTokens;
	}

	public void setInputTokens(long inputTokens) {
		this.inputTokens = inputTokens;
	}

	public long getOutputTokens() {
		return outputTokens;
	}

	public void setOutputTokens(long outputTokens) {
		this.outputTokens = outputTokens;
	}
}
