package com.gateway.patterns.observer;

import com.gateway.model.AuditLog;
import com.gateway.repository.AuditLogRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditLogObserver implements OrderEventObserver {

	private final AuditLogRepository auditLogRepository;

	public AuditLogObserver(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Override
	public void onOrderEvent(String orderId, String eventType, String details) {
		auditLogRepository.save(AuditLog.builder()
				.orderId(orderId)
				.eventType(eventType)
				.details(details)
				.build());
	}
}
