package com.gateway.patterns.memento;

import java.time.LocalDateTime;

import com.gateway.model.Order;

public final class OrderMemento {

	private final String checkpointId;
	private final String orderId;
	private final int version;
	private final String reason;
	private final String status;
	private final LocalDateTime createdAt;
	private final Order snapshot;

	OrderMemento(String orderId, int version, String reason, Order order) {
		this.checkpointId = java.util.UUID.randomUUID().toString();
		this.orderId = orderId;
		this.version = version;
		this.reason = reason;
		this.status = order.getStatus();
		this.createdAt = LocalDateTime.now();
		this.snapshot = order.copy();
	}

	public String getCheckpointId() { return checkpointId; }
	public String getOrderId() { return orderId; }
	public int getVersion() { return version; }
	public String getReason() { return reason; }
	public String getStatus() { return status; }
	public LocalDateTime getCreatedAt() { return createdAt; }

	Order restoreSnapshot() {
		return snapshot.copy();
	}
}
