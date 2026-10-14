package com.gateway.patterns.prototype;

import com.gateway.model.Order;

public final class SavedOrderTemplate implements OrderPrototype {

	private final String templateId;
	private final Order configuration;

	public SavedOrderTemplate(String templateId, Order configuration) {
		if (templateId == null || templateId.isBlank() || configuration == null) {
			throw new IllegalArgumentException("templateId and configuration are required");
		}
		this.templateId = templateId;
		this.configuration = configuration.copy();
		this.configuration.setId(null);
		resetRuntimeState(this.configuration);
	}

	public String getTemplateId() {
		return templateId;
	}

	@Override
	public Order cloneForNewOrder(String newOrderId) {
		if (newOrderId == null || newOrderId.isBlank()) {
			throw new IllegalArgumentException("newOrderId must not be blank");
		}
		Order clone = configuration.copy();
		clone.setId(newOrderId);
		resetRuntimeState(clone);
		return clone;
	}

	private static void resetRuntimeState(Order order) {
		order.setStatus("RECEIVED");
		order.setShippingLabel(null);
		order.setCustomsInvoice(null);
		order.setShippingCost(null);
		order.setEstimatedDeliveryDays(null);
		order.setCreatedAt(null);
	}
}
