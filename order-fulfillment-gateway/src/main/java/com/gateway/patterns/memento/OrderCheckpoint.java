package com.gateway.patterns.memento;

import java.time.LocalDateTime;

public record OrderCheckpoint(String checkpointId, String orderId, int version,
		String reason, String status, LocalDateTime createdAt) {

	static OrderCheckpoint from(OrderMemento memento) {
		return new OrderCheckpoint(memento.getCheckpointId(), memento.getOrderId(),
				memento.getVersion(), memento.getReason(), memento.getStatus(), memento.getCreatedAt());
	}
}
