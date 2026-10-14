package com.gateway.patterns.builder;

import com.gateway.model.Order;
import com.gateway.model.OrderItem;

public interface OrderBuilder {

	OrderBuilder id(String id);
	OrderBuilder amount(double amount);
	OrderBuilder riskScore(int riskScore);
	OrderBuilder inStock(boolean inStock);
	OrderBuilder authenticated(boolean authenticated);
	OrderBuilder international(boolean international);
	OrderBuilder customer(String name, String email);
	OrderBuilder shippingAddress(String address);
	OrderBuilder paymentMethod(String paymentMethod);
	OrderBuilder deliveryPreference(String preference);
	OrderBuilder notification(String channel, String urgency);
	OrderBuilder addItem(OrderItem item);
	Order build();
}
