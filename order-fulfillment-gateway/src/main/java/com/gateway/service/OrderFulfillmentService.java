package com.gateway.service;

import org.springframework.stereotype.Service;

import com.gateway.model.Order;
import com.gateway.patterns.cor.ValidationContext;
import com.gateway.patterns.cor.ValidationPipeline;
import com.gateway.patterns.cor.ValidationPipelineBuilder;
import com.gateway.patterns.factorymethod.PayPalFactory;
import com.gateway.patterns.factorymethod.PaymentGateway;
import com.gateway.patterns.factorymethod.PaymentProcessorFactory;
import com.gateway.patterns.factorymethod.StripeFactory;
import com.gateway.patterns.proxy.PaymentGatewayProxy;
import com.gateway.patterns.singleton.AppConfigManager;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.repository.OrderRepository;

@Service
public class OrderFulfillmentService {

	private final OrderRepository orderRepository;
	private final OrderStatusSubject statusSubject;
	private final AppConfigManager config = AppConfigManager.getInstance();
	private final ValidationPipeline validationPipeline = new ValidationPipelineBuilder().build();

	public OrderFulfillmentService(OrderRepository orderRepository, OrderStatusSubject statusSubject) {
		this.orderRepository = orderRepository;
		this.statusSubject = statusSubject;
	}

	public void validateOrder(Order order) {
		validationPipeline.validate(new ValidationContext(
				order.isAuthenticated(),
				order.isInStock(),
				order.getRiskScore() <= config.getMaxRiskScore()));
	}

	public String processPayment(Order order, String paymentProvider) {
		PaymentProcessorFactory factory = switch (paymentProvider.toLowerCase()) {
			case "paypal" -> new PayPalFactory();
			case "stripe" -> new StripeFactory();
			default -> throw new IllegalArgumentException("Unsupported payment provider: " + paymentProvider);
		};
		PaymentGateway gateway = factory.createGateway();
		new PaymentGatewayProxy(gateway, config).executePayment(order.getAmount());
		return gateway.getProvider();
	}

	public Order persist(Order order) {
		return orderRepository.save(order);
	}

	public void updateStatus(Order order, String status, String details) {
		order.setStatus(status);
		statusSubject.publishEvent(order.getId(), status, details);
	}

	public void markFailed(Order order, RuntimeException failure) {
		order.setStatus("FAILED");
		orderRepository.save(order);
		statusSubject.publishEvent(order.getId(), "FAILED", failure.getMessage());
	}
}
