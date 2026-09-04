package com.gateway.patterns.bridge.channel;

public class SMSChannel implements MessageChannel {

	@Override
	public void send(String recipient, String message) {
		System.out.printf("SMS to %s: %s%n", recipient, message);
	}
}
