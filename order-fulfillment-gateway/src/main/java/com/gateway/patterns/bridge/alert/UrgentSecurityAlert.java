package com.gateway.patterns.bridge.alert;

import com.gateway.patterns.bridge.channel.MessageChannel;

public class UrgentSecurityAlert extends AlertSender {

	public UrgentSecurityAlert(MessageChannel channel) {
		super(channel);
	}

	@Override
	public void sendAlert(String recipient, String message) {
		channel.send(recipient, "URGENT SECURITY ALERT: " + message);
	}
}
