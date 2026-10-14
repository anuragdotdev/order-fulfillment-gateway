package com.gateway.patterns.strategy;

import com.gateway.model.Order;

public class ExpressShipping implements ShippingStrategy {

	@Override
	public ShippingQuote calculate(Order order) {
		double weight = ShippingRules.packageWeight(order);
		if (!"EXPRESS".equalsIgnoreCase(order.getDeliveryPreference())) {
			throw new IllegalArgumentException("express shipping requires an express delivery preference");
		}
		double internationalSurcharge = order.isInternational() ? 18.00 : 0.0;
		return new ShippingQuote(12.99 + weight * 1.65 + internationalSurcharge,
				order.isInternational() ? 3 : 1, "Express Parcel");
	}
}
