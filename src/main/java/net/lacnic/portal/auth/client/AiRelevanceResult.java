package net.lacnic.portal.auth.client;

import java.io.Serializable;

public class AiRelevanceResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private double relevance;

	public AiRelevanceResult() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}
}
