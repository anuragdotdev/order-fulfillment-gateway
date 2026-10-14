package com.gateway.patterns.strategy;

import com.gateway.model.Order;

final class ShippingRules {

	private ShippingRules() {
	}

	static double packageWeight(Order order) {
		if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
			throw new IllegalArgumentException("shipping requires at least one order item");
		}
		if (order.getShippingAddress() == null || order.getShippingAddress().isBlank()) {
			throw new IllegalArgumentException("shipping address must not be blank");
		}
		return order.getItems().stream().mapToDouble(item -> {
			if (item.getQuantity() <= 0 || !Double.isFinite(item.getUnitWeightKg())
					|| item.getUnitWeightKg() <= 0) {
				throw new IllegalArgumentException("shipping items require positive quantity and weight");
			}
			return item.getQuantity() * item.getUnitWeightKg();
		}).sum();
	}
}
