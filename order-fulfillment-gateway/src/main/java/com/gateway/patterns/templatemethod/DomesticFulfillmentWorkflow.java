package com.gateway.patterns.templatemethod;

import com.gateway.exception.PipelineValidationException;
import com.gateway.model.Order;
import com.gateway.patterns.abstractfactory.FulfillmentDocsFactory;
import com.gateway.patterns.abstractfactory.domestic.DomesticFulfillmentFactory;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.patterns.strategy.ShippingService;
import com.gateway.service.CustomerNotificationService;

public class DomesticFulfillmentWorkflow extends FulfillmentWorkflow {

	public DomesticFulfillmentWorkflow(ShippingService shippingService, OrderStateManager stateManager,
			OrderStatusSubject statusSubject, CustomerNotificationService notificationService) {
		super(shippingService, stateManager, statusSubject, notificationService);
	}

	@Override
	protected void validateDestination(Order order) {
		if (order.isInternational()) {
			throw new PipelineValidationException("Domestic fulfillment requires a domestic destination");
		}
	}

	@Override
	protected FulfillmentDocsFactory documentFactory(Order order) {
		return new DomesticFulfillmentFactory();
	}
}
