package com.gateway.patterns.memento;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.gateway.model.Order;

@Component
public class OrderHistory {

	private final ConcurrentMap<String, CopyOnWriteArrayList<OrderMemento>> checkpoints = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AtomicInteger> versions = new ConcurrentHashMap<>();

	OrderMemento checkpoint(Order order, String reason) {
		int version = versions.computeIfAbsent(order.getId(), ignored -> new AtomicInteger()).incrementAndGet();
		OrderMemento memento = new OrderMemento(order.getId(), version, reason, order);
		checkpoints.computeIfAbsent(order.getId(), ignored -> new CopyOnWriteArrayList<>()).add(memento);
		return memento;
	}

	OrderMemento get(String orderId, String checkpointId) {
		List<OrderMemento> orderCheckpoints = checkpoints.get(orderId);
		if (orderCheckpoints == null) {
			return null;
		}
		return orderCheckpoints.stream()
				.filter(checkpoint -> checkpoint.getCheckpointId().equals(checkpointId))
				.findFirst()
				.orElse(null);
	}

	public List<OrderCheckpoint> list(String orderId) {
		List<OrderMemento> orderCheckpoints = checkpoints.get(orderId);
		if (orderCheckpoints == null) {
			return List.of();
		}
		return orderCheckpoints.stream().map(OrderCheckpoint::from).toList();
	}
}
