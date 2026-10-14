package com.gateway.patterns.strategy;

import com.gateway.model.Order;

public class EconomyShipping implements ShippingStrategy {

	@Override
	public ShippingQuote calculate(Order order) {
		double weight = ShippingRules.packageWeight(order);
		if (order.isInternational() || "EXPRESS".equalsIgnoreCase(order.getDeliveryPreference())) {
			throw new IllegalArgumentException("economy shipping is only available for standard domestic orders");
		}
		return new ShippingQuote(4.99 + weight * 0.75, 5, "Standard Parcel");
	}
}
