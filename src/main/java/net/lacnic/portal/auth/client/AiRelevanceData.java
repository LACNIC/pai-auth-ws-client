package net.lacnic.portal.auth.client;

import java.io.Serializable;
import java.util.List;

public class AiRelevanceData implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean success;
	private int httpStatus;
	private String error;
	private boolean retryable;
	private String sourceId;
	private String useEffective;
	private String model;
	private int batchCount;
	private List<AiRelevanceResult> results = List.of();
	private AiRelevanceUsage usage;
	private Long latencyMs;

	public AiRelevanceData() {
	}

	public AiRelevanceData(String error) {
		this.error = error;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isRetryable() {
		return retryable;
	}

	public void setRetryable(boolean retryable) {
		this.retryable = retryable;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getUseEffective() {
		return useEffective;
	}

	public void setUseEffective(String useEffective) {
		this.useEffective = useEffective;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getBatchCount() {
		return batchCount;
	}

	public void setBatchCount(int batchCount) {
		this.batchCount = batchCount;
	}

	public List<AiRelevanceResult> getResults() {
		return results;
	}

	public void setResults(List<AiRelevanceResult> results) {
		this.results = results;
	}

	public AiRelevanceUsage getUsage() {
		return usage;
	}

	public void setUsage(AiRelevanceUsage usage) {
		this.usage = usage;
	}

	public Long getLatencyMs() {
		return latencyMs;
	}

	public void setLatencyMs(Long latencyMs) {
		this.latencyMs = latencyMs;
	}
}
