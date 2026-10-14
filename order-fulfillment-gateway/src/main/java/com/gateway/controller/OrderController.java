package com.gateway.controller;

import java.util.List;
import java.util.UUID;

import com.gateway.dto.OrderRequest;
import com.gateway.dto.OrderResponse;
import com.gateway.dto.OrderTemplateCloneRequest;
import com.gateway.model.Order;
import com.gateway.model.OrderItem;
import com.gateway.patterns.builder.OrderBuilder;
import com.gateway.patterns.builder.OrderDirector;
import com.gateway.patterns.mediator.OrderMediator;
import com.gateway.patterns.memento.OrderCheckpoint;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.prototype.OrderTemplateRegistry;
import com.gateway.patterns.observer.SseEventHub;
import com.gateway.repository.OrderRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderMediator orderMediator;
	private final OrderRepository orderRepository;
	private final SseEventHub sseEventHub;
	private final OrderTemplateRegistry templateRegistry;
	private final OrderStateManager stateManager;

	public OrderController(OrderMediator orderMediator, OrderRepository orderRepository,
			SseEventHub sseEventHub, OrderTemplateRegistry templateRegistry,
			OrderStateManager stateManager) {
		this.orderMediator = orderMediator;
		this.orderRepository = orderRepository;
		this.sseEventHub = sseEventHub;
		this.templateRegistry = templateRegistry;
		this.stateManager = stateManager;
	}

	@PostMapping("/process")
	public ResponseEntity<OrderResponse> process(@RequestBody OrderRequest request) {
		Order order = buildFromRequest(request);
		Order fulfilledOrder = orderMediator.submit(
				order,
				request.getPaymentProvider() == null ? "stripe" : request.getPaymentProvider(),
				request.getAlertRecipient() == null ? request.getId() : request.getAlertRecipient());
		return ResponseEntity.ok(OrderResponse.from(fulfilledOrder));
	}

	@PostMapping("/templates/{templateId}")
	public ResponseEntity<Void> saveTemplate(@PathVariable String templateId,
			@RequestBody OrderRequest request) {
		templateRegistry.register(templateId, buildFromRequest(request));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/templates/{templateId}/clone")
	public ResponseEntity<OrderResponse> cloneTemplate(
			@PathVariable String templateId,
			@RequestBody(required = false) OrderTemplateCloneRequest overrides) {
		String cloneId = overrides == null || overrides.getOrderId() == null
				|| overrides.getOrderId().isBlank()
				? UUID.randomUUID().toString()
				: overrides.getOrderId();
		Order order = templateRegistry.cloneTemplate(templateId, cloneId);
		if (overrides != null) {
			applyOverrides(order, overrides);
		}
		order = buildFromOrder(order);
		Order processed = orderMediator.submit(order,
				overrides == null ? null : overrides.getPaymentProvider(),
				overrides == null ? null : overrides.getAlertRecipient());
		return ResponseEntity.ok(OrderResponse.from(processed));
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@RequestParam String orderId) {
		return sseEventHub.subscribe(orderId);
	}

	@GetMapping("/{orderId}/checkpoints")
	public List<OrderCheckpoint> checkpoints(
			@PathVariable String orderId) {
		return stateManager.history(orderId);
	}

	@PostMapping("/{orderId}/restore/{checkpointId}")
	public ResponseEntity<OrderResponse> restore(
			@PathVariable String orderId,
			@PathVariable String checkpointId) {
		Order current = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown order: " + orderId));
		Order restored = stateManager.restore(current, checkpointId);
		return ResponseEntity.ok(OrderResponse.from(orderRepository.save(restored)));
	}

	@GetMapping
	public List<Order> getOrders() {
		return orderRepository.findAllByOrderByCreatedAtDesc();
	}

	private Order buildFromRequest(OrderRequest request) {
		String id = request.getId() == null || request.getId().isBlank()
				? UUID.randomUUID().toString() : request.getId();
		String deliveryPreference = valueOr(request.getDeliveryPreference(), "STANDARD");
		validateDeliveryPreference(deliveryPreference);
		String paymentMethod = request.getPaymentProvider() == null
				? "stripe" : request.getPaymentProvider();
		List<OrderItem> items = request.getItems();
		if (items == null || items.isEmpty()) {
			items = List.of(new OrderItem("Order", 1, request.getAmount(), 1.0));
		}
		List<OrderItem> configuredItems = items;
		OrderDirector director = new OrderDirector();
		java.util.function.Consumer<OrderBuilder> configuration = builder -> {
			builder.id(id)
					.amount(request.getAmount())
					.riskScore(request.getRiskScore())
					.inStock(request.isInStock())
					.authenticated(request.isAuthenticated())
					.international(request.isInternational())
					.customer(valueOr(request.getCustomerName(), "Guest"), request.getCustomerEmail())
					.shippingAddress(valueOr(request.getShippingAddress(), "Not provided"))
					.paymentMethod(paymentMethod)
					.notification(valueOr(request.getNotificationChannel(), "ALL"),
							valueOr(request.getAlertUrgency(), "STANDARD"));
			configuredItems.forEach(builder::addItem);
		};
		if ("EXPRESS".equalsIgnoreCase(deliveryPreference)) {
			return director.expressOrder(configuration);
		}
		return director.standardOrder(builder -> {
			configuration.accept(builder);
			builder.deliveryPreference(deliveryPreference);
		});
	}

	private Order buildFromOrder(Order order) {
		validateDeliveryPreference(order.getDeliveryPreference());
		OrderDirector director = new OrderDirector();
		java.util.function.Consumer<OrderBuilder> configuration = builder -> {
			builder.id(order.getId())
					.amount(order.getAmount())
					.riskScore(order.getRiskScore())
					.inStock(order.isInStock())
					.authenticated(order.isAuthenticated())
					.international(order.isInternational())
					.customer(order.getCustomerName(), order.getCustomerEmail())
					.shippingAddress(order.getShippingAddress())
					.paymentMethod(order.getPaymentMethod())
					.notification(order.getNotificationChannel(), order.getAlertUrgency());
			if (order.getItems() != null) {
				order.getItems().forEach(builder::addItem);
			}
		};
		return "EXPRESS".equalsIgnoreCase(order.getDeliveryPreference())
				? director.expressOrder(configuration)
				: director.standardOrder(configuration);
	}

	private void applyOverrides(Order order, OrderTemplateCloneRequest overrides) {
		if (overrides.getAmount() != null) order.setAmount(overrides.getAmount());
		if (overrides.getRiskScore() != null) order.setRiskScore(overrides.getRiskScore());
		if (overrides.getInStock() != null) order.setInStock(overrides.getInStock());
		if (overrides.getAuthenticated() != null) order.setAuthenticated(overrides.getAuthenticated());
		if (overrides.getInternational() != null) order.setInternational(overrides.getInternational());
		if (overrides.getCustomerName() != null) order.setCustomerName(overrides.getCustomerName());
		if (overrides.getCustomerEmail() != null) order.setCustomerEmail(overrides.getCustomerEmail());
		if (overrides.getShippingAddress() != null) order.setShippingAddress(overrides.getShippingAddress());
		if (overrides.getPaymentMethod() != null) order.setPaymentMethod(overrides.getPaymentMethod());
		if (overrides.getDeliveryPreference() != null) order.setDeliveryPreference(overrides.getDeliveryPreference());
		if (overrides.getNotificationChannel() != null) order.setNotificationChannel(overrides.getNotificationChannel());
		if (overrides.getAlertUrgency() != null) order.setAlertUrgency(overrides.getAlertUrgency());
		if (overrides.getItems() != null) {
			order.setItems(new java.util.ArrayList<>(overrides.getItems().stream().map(OrderItem::copy).toList()));
		}
	}

	private static String valueOr(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private static void validateDeliveryPreference(String preference) {
		if (!"STANDARD".equalsIgnoreCase(preference) && !"EXPRESS".equalsIgnoreCase(preference)) {
			throw new IllegalArgumentException("deliveryPreference must be STANDARD or EXPRESS");
		}
	}
}
