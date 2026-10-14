package com.gateway.service;

import org.springframework.stereotype.Service;

import com.gateway.model.Order;
import com.gateway.patterns.bridge.alert.AlertSender;
import com.gateway.patterns.bridge.alert.StandardOrderAlert;
import com.gateway.patterns.bridge.alert.UrgentSecurityAlert;
import com.gateway.patterns.bridge.channel.MessageChannel;
import com.gateway.patterns.bridge.channel.SMSChannel;
import com.gateway.patterns.bridge.channel.WebhookChannel;

@Service
public class CustomerNotificationService {

	public void sendFulfillmentConfirmation(Order order, String recipient) {
		if (order == null) {
			throw new IllegalArgumentException("order must not be null");
		}
		String channelName = order.getNotificationChannel() == null
				? "ALL" : order.getNotificationChannel();
		String destination = recipient == null || recipient.isBlank() ? order.getId() : recipient;
		if ("ALL".equalsIgnoreCase(channelName)) {
			new StandardOrderAlert(new SMSChannel()).sendAlert(destination,
					"Order " + order.getId() + " is processing");
			new UrgentSecurityAlert(new WebhookChannel()).sendAlert(destination,
					"Order " + order.getId() + " passed security checks");
			return;
		}
		MessageChannel channel = switch (channelName.toUpperCase()) {
			case "SMS" -> new SMSChannel();
			case "WEBHOOK" -> new WebhookChannel();
			default -> throw new IllegalArgumentException("Unsupported notification channel: " + channelName);
		};
		AlertSender alert = "URGENT".equalsIgnoreCase(order.getAlertUrgency())
				? new UrgentSecurityAlert(channel) : new StandardOrderAlert(channel);
		alert.sendAlert(destination, "Order " + order.getId() + " fulfillment completed");
	}
}
