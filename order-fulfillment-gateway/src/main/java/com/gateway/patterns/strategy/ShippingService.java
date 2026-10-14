package com.gateway.patterns.strategy;

import com.gateway.model.Order;

public class ShippingService {

	private volatile ShippingStrategy strategy;

	public ShippingService(ShippingStrategy strategy) {
		setStrategy(strategy);
	}

	public void setStrategy(ShippingStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("strategy must not be null");
		}
		this.strategy = strategy;
	}

	public ShippingQuote calculate(Order order) {
		return strategy.calculate(order);
	}
}
