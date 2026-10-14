package com.gateway.patterns.builder;

import java.util.ArrayList;
import java.util.List;

import com.gateway.model.Order;
import com.gateway.model.OrderItem;

public class StandardOrderBuilder implements OrderBuilder {

	private String id;
	private Double amount;
	private int riskScore;
	private boolean inStock;
	private boolean authenticated;
	private boolean international;
	private String customerName;
	private String customerEmail;
	private String shippingAddress;
	private String paymentMethod;
	private String deliveryPreference = "STANDARD";
	private String notificationChannel = "ALL";
	private String alertUrgency = "STANDARD";
	private final List<OrderItem> items = new ArrayList<>();

	@Override public OrderBuilder id(String value) { id = value; return this; }
	@Override public OrderBuilder amount(double value) { amount = value; return this; }
	@Override public OrderBuilder riskScore(int value) { riskScore = value; return this; }
	@Override public OrderBuilder inStock(boolean value) { inStock = value; return this; }
	@Override public OrderBuilder authenticated(boolean value) { authenticated = value; return this; }
	@Override public OrderBuilder international(boolean value) { international = value; return this; }
	@Override public OrderBuilder customer(String name, String email) {
		customerName = name;
		customerEmail = email;
		return this;
	}
	@Override public OrderBuilder shippingAddress(String value) { shippingAddress = value; return this; }
	@Override public OrderBuilder paymentMethod(String value) { paymentMethod = value; return this; }
	@Override public OrderBuilder deliveryPreference(String value) { deliveryPreference = value; return this; }
	@Override public OrderBuilder notification(String channel, String urgency) {
		notificationChannel = channel;
		alertUrgency = urgency;
		return this;
	}
	@Override public OrderBuilder addItem(OrderItem item) {
		if (item == null) {
			throw new IllegalArgumentException("item must not be null");
		}
		items.add(item.copy());
		return this;
	}

	@Override
	public Order build() {
		requireText(id, "id");
		requireText(customerName, "customerName");
		requireText(shippingAddress, "shippingAddress");
		requireText(paymentMethod, "paymentMethod");
		requireText(deliveryPreference, "deliveryPreference");
		requireText(notificationChannel, "notificationChannel");
		requireText(alertUrgency, "alertUrgency");
		if (!"STANDARD".equalsIgnoreCase(deliveryPreference)
				&& !"EXPRESS".equalsIgnoreCase(deliveryPreference)) {
			throw new IllegalArgumentException("deliveryPreference must be STANDARD or EXPRESS");
		}
		if (riskScore < 0 || riskScore > 100) {
			throw new IllegalArgumentException("riskScore must be between 0 and 100");
		}
		if (items.isEmpty()) {
			throw new IllegalArgumentException("at least one order item is required");
		}
		for (OrderItem item : items) {
			if (item.getName() == null || item.getName().isBlank()
					|| item.getQuantity() <= 0 || item.getUnitPrice() < 0
					|| item.getUnitWeightKg() <= 0) {
				throw new IllegalArgumentException("order items require a name, positive quantity and weight, and non-negative price");
			}
		}
		double itemTotal = items.stream().mapToDouble(item -> item.getQuantity() * item.getUnitPrice()).sum();
		double orderAmount = amount == null ? itemTotal : amount;
		if (!Double.isFinite(orderAmount) || orderAmount <= 0) {
			throw new IllegalArgumentException("amount must be greater than zero");
		}
		return Order.builder()
				.id(id)
				.amount(orderAmount)
				.riskScore(riskScore)
				.inStock(inStock)
				.authenticated(authenticated)
				.international(international)
				.status("RECEIVED")
				.items(new ArrayList<>(items.stream().map(OrderItem::copy).toList()))
				.customerName(customerName)
				.customerEmail(customerEmail)
				.shippingAddress(shippingAddress)
				.paymentMethod(paymentMethod)
				.deliveryPreference(deliveryPreference)
				.notificationChannel(notificationChannel)
				.alertUrgency(alertUrgency)
				.build();
	}

	private static void requireText(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
	}
}
