package com.gateway.patterns.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusSubject {

	private final List<OrderEventObserver> observers = new CopyOnWriteArrayList<>();

	public OrderStatusSubject() {
	}

	@Autowired
	public OrderStatusSubject(List<OrderEventObserver> observers) {
		this.observers.addAll(observers);
	}

	public void addObserver(OrderEventObserver observer) {
		if (observer == null) {
			throw new IllegalArgumentException("observer must not be null");
		}
		observers.add(observer);
	}

	public void removeObserver(OrderEventObserver observer) {
		observers.remove(observer);
	}

	public void publishEvent(String orderId, String eventType, String details) {
		for (OrderEventObserver observer : observers) {
			observer.onOrderEvent(orderId, eventType, details);
		}
	}
}
