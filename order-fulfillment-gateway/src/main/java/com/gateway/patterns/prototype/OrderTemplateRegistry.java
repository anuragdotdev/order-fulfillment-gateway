package com.gateway.patterns.prototype;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.gateway.model.Order;

@Component
public class OrderTemplateRegistry {

	private final ConcurrentMap<String, SavedOrderTemplate> templates = new ConcurrentHashMap<>();

	public void register(String templateId, Order configuration) {
		SavedOrderTemplate template = new SavedOrderTemplate(templateId, configuration);
		templates.put(templateId, template);
	}

	public SavedOrderTemplate get(String templateId) {
		SavedOrderTemplate template = templates.get(templateId);
		if (template == null) {
			throw new IllegalArgumentException("Unknown order template: " + templateId);
		}
		return template;
	}

	public Order cloneTemplate(String templateId) {
		return cloneTemplate(templateId, UUID.randomUUID().toString());
	}

	public Order cloneTemplate(String templateId, String newOrderId) {
		return get(templateId).cloneForNewOrder(newOrderId);
	}
}
