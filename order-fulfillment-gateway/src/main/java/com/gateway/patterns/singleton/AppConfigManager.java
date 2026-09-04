package com.gateway.patterns.singleton;

public class AppConfigManager {

	private static final int DEFAULT_MAX_RISK_SCORE = 100;
	private static final long DEFAULT_API_TIMEOUT_MILLIS = 5_000L;
	private static final int DEFAULT_RATE_LIMIT_PER_MINUTE = 100;

	private volatile int maxRiskScore = DEFAULT_MAX_RISK_SCORE;
	private volatile long apiTimeoutMillis = DEFAULT_API_TIMEOUT_MILLIS;
	private volatile int rateLimitPerMinute = DEFAULT_RATE_LIMIT_PER_MINUTE;

	private AppConfigManager() {
	}

	private static class Holder {
		private static final AppConfigManager INSTANCE = new AppConfigManager();
	}

	public static AppConfigManager getInstance() {
		return Holder.INSTANCE;
	}

	public int getMaxRiskScore() {
		return maxRiskScore;
	}

	public synchronized void setMaxRiskScore(int maxRiskScore) {
		validateNonNegative(maxRiskScore, "maxRiskScore");
		this.maxRiskScore = maxRiskScore;
	}

	public long getApiTimeoutMillis() {
		return apiTimeoutMillis;
	}

	public synchronized void setApiTimeoutMillis(long apiTimeoutMillis) {
		validatePositive(apiTimeoutMillis, "apiTimeoutMillis");
		this.apiTimeoutMillis = apiTimeoutMillis;
	}

	public int getRateLimitPerMinute() {
		return rateLimitPerMinute;
	}

	public synchronized void setRateLimitPerMinute(int rateLimitPerMinute) {
		validatePositive(rateLimitPerMinute, "rateLimitPerMinute");
		this.rateLimitPerMinute = rateLimitPerMinute;
	}

	public synchronized void updateConfiguration(int maxRiskScore,
			long apiTimeoutMillis, int rateLimitPerMinute) {
		validateNonNegative(maxRiskScore, "maxRiskScore");
		validatePositive(apiTimeoutMillis, "apiTimeoutMillis");
		validatePositive(rateLimitPerMinute, "rateLimitPerMinute");
		this.maxRiskScore = maxRiskScore;
		this.apiTimeoutMillis = apiTimeoutMillis;
		this.rateLimitPerMinute = rateLimitPerMinute;
	}

	public synchronized Configuration getConfiguration() {
		return new Configuration(maxRiskScore, apiTimeoutMillis, rateLimitPerMinute);
	}

	private static void validatePositive(long value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}

	private static void validateNonNegative(long value, String name) {
		if (value < 0) {
			throw new IllegalArgumentException(name + " must not be negative");
		}
	}

	public record Configuration(int maxRiskScore, long apiTimeoutMillis, int rateLimitPerMinute) {
	}
}
