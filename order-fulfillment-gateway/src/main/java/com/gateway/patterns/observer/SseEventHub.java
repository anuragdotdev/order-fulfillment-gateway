package com.gateway.patterns.observer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEventHub implements OrderEventObserver {

	private final ConcurrentMap<String, CopyOnWriteArrayList<SseEmitter>> emittersByOrder =
			new ConcurrentHashMap<>();

	public SseEmitter subscribe(String orderId) {
		if (orderId == null || orderId.isBlank()) {
			throw new IllegalArgumentException("orderId must not be blank");
		}

		SseEmitter emitter = new SseEmitter();
		emittersByOrder.computeIfAbsent(orderId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
		Runnable cleanup = () -> removeEmitter(orderId, emitter);
		emitter.onCompletion(cleanup);
		emitter.onTimeout(cleanup);
		emitter.onError(ignored -> cleanup.run());
		return emitter;
	}

	@Override
	public void onOrderEvent(String orderId, String eventType, String details) {
		CopyOnWriteArrayList<SseEmitter> emitters = emittersByOrder.get(orderId);
		if (emitters == null) {
			return;
		}

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
						.name(eventType)
						.data(details));
			} catch (IOException exception) {
				removeEmitter(orderId, emitter);
			}
		}
	}

	private void removeEmitter(String orderId, SseEmitter emitter) {
		CopyOnWriteArrayList<SseEmitter> emitters = emittersByOrder.get(orderId);
		if (emitters != null && emitters.remove(emitter) && emitters.isEmpty()) {
			emittersByOrder.remove(orderId, emitters);
		}
	}
}
