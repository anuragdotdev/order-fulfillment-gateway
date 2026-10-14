package com.gateway.patterns.mediator;

import org.springframework.stereotype.Component;

import com.gateway.model.Order;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.templatemethod.FulfillmentWorkflowFactory;
import com.gateway.service.OrderFulfillmentService;

@Component
public class OrderWorkflowMediator implements OrderMediator {

	private final OrderFulfillmentService fulfillmentService;
	private final FulfillmentWorkflowFactory workflowFactory;
	private final OrderStateManager stateManager;

	public OrderWorkflowMediator(OrderFulfillmentService fulfillmentService,
			FulfillmentWorkflowFactory workflowFactory, OrderStateManager stateManager) {
		this.fulfillmentService = fulfillmentService;
		this.workflowFactory = workflowFactory;
		this.stateManager = stateManager;
	}

	@Override
	public Order submit(Order order, String paymentProvider, String alertRecipient) {
		if (order == null || order.getId() == null || order.getId().isBlank()) {
			throw new IllegalArgumentException("order and order id are required");
		}
		if (!"RECEIVED".equals(order.getStatus())) {
			throw new IllegalStateException("only a newly received order can enter the workflow");
		}
		String provider = paymentProvider == null || paymentProvider.isBlank()
				? order.getPaymentMethod() : paymentProvider;
		if (provider == null || provider.isBlank()) {
			provider = "stripe";
		}
		try {
			fulfillmentService.updateStatus(order, "RECEIVED", "Order received");
			stateManager.checkpoint(order, "before-validation");
			fulfillmentService.validateOrder(order);
			fulfillmentService.updateStatus(order, "VALIDATED", "Order passed validation");
			stateManager.checkpoint(order, "before-payment");

			stateManager.markExternalOperationStarted(order.getId(), "payment");
			String actualProvider = fulfillmentService.processPayment(order, provider);
			fulfillmentService.updateStatus(order, "PAYMENT_PROCESSED",
					actualProvider + " payment processed");

			workflowFactory.forOrder(order).fulfill(order, alertRecipient);
			return fulfillmentService.persist(order);
		} catch (RuntimeException failure) {
			fulfillmentService.markFailed(order, failure);
			throw failure;
		}
	}
}
