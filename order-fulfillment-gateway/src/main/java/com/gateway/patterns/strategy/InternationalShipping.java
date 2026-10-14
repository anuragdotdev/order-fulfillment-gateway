package com.gateway.patterns.strategy;

import com.gateway.model.Order;

public class InternationalShipping implements ShippingStrategy {

	@Override
	public ShippingQuote calculate(Order order) {
		double weight = ShippingRules.packageWeight(order);
		if (!order.isInternational()) {
			throw new IllegalArgumentException("international shipping requires an international destination");
		}
		if ("EXPRESS".equalsIgnoreCase(order.getDeliveryPreference())) {
			throw new IllegalArgumentException("express orders require the express shipping strategy");
		}
		return new ShippingQuote(24.99 + weight * 3.25, 10, "International Parcel");
	}
}
