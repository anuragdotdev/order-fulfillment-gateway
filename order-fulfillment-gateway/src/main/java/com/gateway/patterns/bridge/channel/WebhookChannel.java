package com.gateway.patterns.bridge.channel;

public class WebhookChannel implements MessageChannel {

	@Override
	public void send(String recipient, String message) {
		System.out.printf("Webhook to %s: %s%n", recipient, message);
	}
}
