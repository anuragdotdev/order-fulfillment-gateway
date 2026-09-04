package com.gateway.patterns.observer;

public interface OrderEventObserver {

	void onOrderEvent(String orderId, String eventType, String details);
}
