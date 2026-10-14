package com.gateway.patterns.strategy;

public record ShippingQuote(double cost, int estimatedDeliveryDays, String carrier) {

	public ShippingQuote {
		if (!Double.isFinite(cost) || cost < 0 || estimatedDeliveryDays <= 0
				|| carrier == null || carrier.isBlank()) {
			throw new IllegalArgumentException("shipping quote must contain valid cost, delivery time, and carrier");
		}
	}
}
