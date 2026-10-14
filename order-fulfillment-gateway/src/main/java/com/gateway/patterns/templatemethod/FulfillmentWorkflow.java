package com.gateway.patterns.templatemethod;

import com.gateway.model.Order;
import com.gateway.patterns.abstractfactory.FulfillmentDocsFactory;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.patterns.strategy.ShippingQuote;
import com.gateway.patterns.strategy.ShippingService;
import com.gateway.service.CustomerNotificationService;

public abstract class FulfillmentWorkflow {

	private final ShippingService shippingService;
	private final OrderStateManager stateManager;
	private final OrderStatusSubject statusSubject;
	private final CustomerNotificationService notificationService;

	protected FulfillmentWorkflow(ShippingService shippingService, OrderStateManager stateManager,
			OrderStatusSubject statusSubject, CustomerNotificationService notificationService) {
		this.shippingService = shippingService;
		this.stateManager = stateManager;
		this.statusSubject = statusSubject;
		this.notificationService = notificationService;
	}

	public final void fulfill(Order order, String alertRecipient) {
		validateDestination(order);
		stateManager.checkpoint(order, "before-shipping-calculation");
		ShippingQuote quote = shippingService.calculate(order);
		order.setShippingCost(quote.cost());
		order.setEstimatedDeliveryDays(quote.estimatedDeliveryDays());
		updateStatus(order, "SHIPPING_CALCULATED",
				quote.carrier() + " estimate: " + quote.estimatedDeliveryDays() + " days");
		stateManager.checkpoint(order, "before-document-generation");
		FulfillmentDocsFactory docs = documentFactory(order);
		order.setShippingLabel(docs.createShippingLabel().getDocumentType());
		order.setCustomsInvoice(docs.createCustomsInvoice().getDocumentType());
		stateManager.checkpoint(order, "before-shipment-dispatch");
		updateStatus(order, "DOCUMENTS_GENERATED", "Fulfillment documents generated");
		stateManager.markExternalOperationStarted(order.getId(), "shipment dispatch");
		dispatchShipment(order, quote);
		updateStatus(order, "FULFILLMENT_DISPATCHED",
				"Shipment dispatched via " + quote.carrier());
		stateManager.markExternalOperationStarted(order.getId(), "customer notification");
		notificationService.sendFulfillmentConfirmation(order, alertRecipient);
		updateStatus(order, "COMPLETED", "Order fulfillment completed");
	}

	protected abstract void validateDestination(Order order);

	protected abstract FulfillmentDocsFactory documentFactory(Order order);

	protected void dispatchShipment(Order order, ShippingQuote quote) {
		if (quote == null || order.getShippingLabel() == null) {
			throw new IllegalStateException("shipment cannot be dispatched without a quote and shipping label");
		}
	}

	private void updateStatus(Order order, String status, String details) {
		order.setStatus(status);
		statusSubject.publishEvent(order.getId(), status, details);
	}
}
