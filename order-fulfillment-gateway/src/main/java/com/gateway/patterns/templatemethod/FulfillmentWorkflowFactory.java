package com.gateway.patterns.templatemethod;

import org.springframework.stereotype.Component;

import com.gateway.model.Order;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.patterns.strategy.EconomyShipping;
import com.gateway.patterns.strategy.ExpressShipping;
import com.gateway.patterns.strategy.InternationalShipping;
import com.gateway.patterns.strategy.ShippingService;
import com.gateway.service.CustomerNotificationService;

@Component
public class FulfillmentWorkflowFactory {

	private final OrderStateManager stateManager;
	private final OrderStatusSubject statusSubject;
	private final CustomerNotificationService notificationService;

	public FulfillmentWorkflowFactory(OrderStateManager stateManager,
			OrderStatusSubject statusSubject, CustomerNotificationService notificationService) {
		this.stateManager = stateManager;
		this.statusSubject = statusSubject;
		this.notificationService = notificationService;
	}

	public FulfillmentWorkflow forOrder(Order order) {
		if ("EXPRESS".equalsIgnoreCase(order.getDeliveryPreference())) {
			return new ExpressFulfillmentWorkflow(new ShippingService(new ExpressShipping()),
					stateManager, statusSubject, notificationService);
		}
		if (order.isInternational()) {
			return new InternationalFulfillmentWorkflow(new ShippingService(new InternationalShipping()),
					stateManager, statusSubject, notificationService);
		}
		return new DomesticFulfillmentWorkflow(new ShippingService(new EconomyShipping()),
				stateManager, statusSubject, notificationService);
	}
}
