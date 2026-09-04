package com.gateway.controller;

import java.util.List;

import com.gateway.dto.OrderRequest;
import com.gateway.dto.OrderResponse;
import com.gateway.model.Order;
import com.gateway.patterns.observer.SseEventHub;
import com.gateway.repository.OrderRepository;
import com.gateway.service.OrderFulfillmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderFulfillmentService fulfillmentService;
	private final OrderRepository orderRepository;
	private final SseEventHub sseEventHub;

	public OrderController(OrderFulfillmentService fulfillmentService,
			OrderRepository orderRepository, SseEventHub sseEventHub) {
		this.fulfillmentService = fulfillmentService;
		this.orderRepository = orderRepository;
		this.sseEventHub = sseEventHub;
	}

	@PostMapping("/process")
	public ResponseEntity<OrderResponse> process(@RequestBody OrderRequest request) {
		Order order = Order.builder()
				.id(request.getId())
				.amount(request.getAmount())
				.riskScore(request.getRiskScore())
				.inStock(request.isInStock())
				.authenticated(request.isAuthenticated())
				.international(request.isInternational())
				.build();
		Order fulfilledOrder = fulfillmentService.fulfillOrder(
				order,
				request.getPaymentProvider() == null ? "stripe" : request.getPaymentProvider(),
				request.getAlertRecipient() == null ? request.getId() : request.getAlertRecipient());
		return ResponseEntity.ok(OrderResponse.from(fulfilledOrder));
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@RequestParam String orderId) {
		return sseEventHub.subscribe(orderId);
	}

	@GetMapping
	public List<Order> getOrders() {
		return orderRepository.findAllByOrderByCreatedAtDesc();
	}
}
