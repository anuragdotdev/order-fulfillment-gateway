package com.gateway.patterns.memento;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.gateway.model.Order;

@Component
public class OrderStateManager {

	private final OrderHistory history;
	private final ConcurrentMap<String, String> externalOperationsStarted = new ConcurrentHashMap<>();

	public OrderStateManager(OrderHistory history) {
		this.history = history;
	}

	public OrderCheckpoint checkpoint(Order order, String reason) {
		if (order == null || order.getId() == null || order.getId().isBlank()
				|| reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("order, order id, and checkpoint reason are required");
		}
		return OrderCheckpoint.from(history.checkpoint(order, reason));
	}

	public List<OrderCheckpoint> history(String orderId) {
		return history.list(orderId);
	}

	public void markExternalOperationStarted(String orderId, String operation) {
		if (orderId == null || orderId.isBlank() || operation == null || operation.isBlank()) {
			throw new IllegalArgumentException("order id and external operation are required");
		}
		externalOperationsStarted.putIfAbsent(orderId, operation);
	}

	public Order restore(Order current, String checkpointId) {
		if (current == null || current.getId() == null || checkpointId == null || checkpointId.isBlank()) {
			throw new IllegalArgumentException("current order and checkpoint id are required");
		}
		if (externalOperationsStarted.containsKey(current.getId())) {
			throw new IllegalStateException("order cannot be restored after an external operation started");
		}
		if (!"RECEIVED".equals(current.getStatus()) && !"VALIDATED".equals(current.getStatus())
				&& !"FAILED".equals(current.getStatus())) {
			throw new IllegalStateException("orders cannot be restored after payment or fulfillment begins");
		}
		OrderMemento memento = history.get(current.getId(), checkpointId);
		if (memento == null) {
			throw new IllegalArgumentException("checkpoint does not exist for this order");
		}
		if (!"RECEIVED".equals(memento.getStatus()) && !"VALIDATED".equals(memento.getStatus())) {
			throw new IllegalStateException("checkpoint contains state after external processing began");
		}
		return memento.restoreSnapshot();
	}
}
