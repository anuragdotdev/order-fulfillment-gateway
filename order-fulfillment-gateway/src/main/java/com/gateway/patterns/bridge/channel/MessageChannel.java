package com.gateway.patterns.bridge.channel;

public interface MessageChannel {

	void send(String recipient, String message);
}
