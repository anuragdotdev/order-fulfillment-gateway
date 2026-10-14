package com.gateway.patterns.templatemethod;

import com.gateway.model.Order;
import com.gateway.patterns.abstractfactory.FulfillmentDocsFactory;
import com.gateway.patterns.abstractfactory.domestic.DomesticFulfillmentFactory;
import com.gateway.patterns.abstractfactory.international.InternationalFulfillmentFactory;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.patterns.strategy.ShippingService;
import com.gateway.service.CustomerNotificationService;

public class ExpressFulfillmentWorkflow extends FulfillmentWorkflow {

	public ExpressFulfillmentWorkflow(ShippingService shippingService, OrderStateManager stateManager,
			OrderStatusSubject statusSubject, CustomerNotificationService notificationService) {
		super(shippingService, stateManager, statusSubject, notificationService);
	}

	@Override
	protected void validateDestination(Order order) {
		if (!"EXPRESS".equalsIgnoreCase(order.getDeliveryPreference())) {
			throw new IllegalArgumentException("express fulfillment requires an express delivery preference");
		}
	}

	@Override
	protected FulfillmentDocsFactory documentFactory(Order order) {
		return order.isInternational()
				? new InternationalFulfillmentFactory()
				: new DomesticFulfillmentFactory();
	}
}
