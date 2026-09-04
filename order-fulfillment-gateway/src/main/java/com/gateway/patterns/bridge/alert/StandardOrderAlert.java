package com.gateway.patterns.bridge.alert;

import com.gateway.patterns.bridge.channel.MessageChannel;

public class StandardOrderAlert extends AlertSender {

	public StandardOrderAlert(MessageChannel channel) {
		super(channel);
	}

	@Override
	public void sendAlert(String recipient, String message) {
		channel.send(recipient, "Order alert: " + message);
	}
}
