package com.gateway.patterns.strategy;

import com.gateway.model.Order;

public interface ShippingStrategy {

	ShippingQuote calculate(Order order);
}
