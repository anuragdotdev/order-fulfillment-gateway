package com.gateway.patterns.builder;

import com.gateway.model.Order;

public class ExpressOrderBuilder extends StandardOrderBuilder {

	public ExpressOrderBuilder() {
		deliveryPreference("EXPRESS");
	}

	@Override
	public Order build() {
		deliveryPreference("EXPRESS");
		return super.build();
	}
}
