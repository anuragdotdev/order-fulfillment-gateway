package com.gateway.patterns.bridge.alert;

import com.gateway.patterns.bridge.channel.MessageChannel;

public abstract class AlertSender {

	protected final MessageChannel channel;

	protected AlertSender(MessageChannel channel) {
		if (channel == null) {
			throw new IllegalArgumentException("channel must not be null");
		}
		this.channel = channel;
	}

	public abstract void sendAlert(String recipient, String message);
}
