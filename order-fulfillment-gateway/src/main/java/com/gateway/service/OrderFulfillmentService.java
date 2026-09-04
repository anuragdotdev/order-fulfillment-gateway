package com.gateway.service;

import com.gateway.model.Order;
import com.gateway.patterns.abstractfactory.CustomsInvoice;
import com.gateway.patterns.abstractfactory.FulfillmentDocsFactory;
import com.gateway.patterns.abstractfactory.ShippingLabel;
import com.gateway.patterns.abstractfactory.domestic.DomesticFulfillmentFactory;
import com.gateway.patterns.abstractfactory.international.InternationalFulfillmentFactory;
import com.gateway.patterns.bridge.alert.AlertSender;
import com.gateway.patterns.bridge.alert.StandardOrderAlert;
import com.gateway.patterns.bridge.alert.UrgentSecurityAlert;
import com.gateway.patterns.bridge.channel.SMSChannel;
import com.gateway.patterns.bridge.channel.WebhookChannel;
import com.gateway.patterns.cor.ValidationContext;
import com.gateway.patterns.cor.ValidationPipeline;
import com.gateway.patterns.cor.ValidationPipelineBuilder;
import com.gateway.patterns.factorymethod.PayPalFactory;
import com.gateway.patterns.factorymethod.PaymentGateway;
import com.gateway.patterns.factorymethod.PaymentProcessorFactory;
import com.gateway.patterns.factorymethod.StripeFactory;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.patterns.proxy.PaymentGatewayProxy;
import com.gateway.patterns.singleton.AppConfigManager;
import com.gateway.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderFulfillmentService {

	private final OrderRepository orderRepository;
	private final OrderStatusSubject statusSubject;
	private final AppConfigManager config = AppConfigManager.getInstance();
	private final ValidationPipeline validationPipeline = new ValidationPipelineBuilder().build();
	private final AlertSender standardAlert = new StandardOrderAlert(new SMSChannel());
	private final AlertSender urgentAlert = new UrgentSecurityAlert(new WebhookChannel());

	public OrderFulfillmentService(OrderRepository orderRepository, OrderStatusSubject statusSubject) {
		this.orderRepository = orderRepository;
		this.statusSubject = statusSubject;
	}

	public Order fulfillOrder(Order order) {
		if (order == null) {
			throw new IllegalArgumentException("order must not be null");
		}
		return fulfillOrder(order, "stripe", order.getId());
	}

	public Order fulfillOrder(Order order, String paymentProvider, String alertRecipient) {
		if (order == null) {
			throw new IllegalArgumentException("order must not be null");
		}

		try {
			updateStatus(order, "RECEIVED", "Order received");
			validateOrder(order);
			updateStatus(order, "VALIDATED", "Order passed validation");

			PaymentGateway gateway = createPaymentGateway(paymentProvider);
			new PaymentGatewayProxy(gateway, config).executePayment(order.getAmount());
			updateStatus(order, "PAYMENT_PROCESSED", gateway.getProvider() + " payment processed");

			FulfillmentDocsFactory docsFactory = order.isInternational()
					? new InternationalFulfillmentFactory()
					: new DomesticFulfillmentFactory();
			ShippingLabel label = docsFactory.createShippingLabel();
			CustomsInvoice invoice = docsFactory.createCustomsInvoice();
			order.setShippingLabel(label.getDocumentType());
			order.setCustomsInvoice(invoice.getDocumentType());
			updateStatus(order, "DOCUMENTS_GENERATED", "Fulfillment documents generated");

			standardAlert.sendAlert(alertRecipient, "Order " + order.getId() + " is processing");
			urgentAlert.sendAlert(alertRecipient, "Order " + order.getId() + " passed security checks");
			updateStatus(order, "COMPLETED", "Order fulfillment completed");
			return orderRepository.save(order);
		} catch (RuntimeException exception) {
			order.setStatus("FAILED");
			orderRepository.save(order);
			statusSubject.publishEvent(order.getId(), "FAILED", exception.getMessage());
			throw exception;
		}
	}

	private void validateOrder(Order order) {
		validationPipeline.validate(new ValidationContext(
				order.isAuthenticated(),
				order.isInStock(),
				order.getRiskScore() <= config.getMaxRiskScore()));
	}

	private PaymentGateway createPaymentGateway(String paymentProvider) {
		if (paymentProvider == null) {
			throw new IllegalArgumentException("paymentProvider must not be null");
		}
		PaymentProcessorFactory factory = switch (paymentProvider.toLowerCase()) {
			case "paypal" -> new PayPalFactory();
			case "stripe" -> new StripeFactory();
			default -> throw new IllegalArgumentException("Unsupported payment provider: " + paymentProvider);
		};
		return factory.createGateway();
	}

	private void updateStatus(Order order, String status, String details) {
		order.setStatus(status);
		statusSubject.publishEvent(order.getId(), status, details);
	}
}
