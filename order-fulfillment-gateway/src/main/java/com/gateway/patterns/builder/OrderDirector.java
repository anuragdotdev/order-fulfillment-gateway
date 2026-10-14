package com.gateway.patterns.builder;

import java.util.function.Consumer;

import com.gateway.model.Order;

public class OrderDirector {

	public Order standardOrder(Consumer<OrderBuilder> configuration) {
		return build(new StandardOrderBuilder(), "STANDARD", configuration);
	}

	public Order expressOrder(Consumer<OrderBuilder> configuration) {
		return build(new ExpressOrderBuilder(), "EXPRESS", configuration);
	}

	private Order build(OrderBuilder builder, String preference, Consumer<OrderBuilder> configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException("configuration must not be null");
		}
		configuration.accept(builder);
		builder.deliveryPreference(preference);
		return builder.build();
	}
}
