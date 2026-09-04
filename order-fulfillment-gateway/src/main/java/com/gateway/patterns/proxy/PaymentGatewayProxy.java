package com.gateway.patterns.proxy;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import com.gateway.patterns.factorymethod.PaymentGateway;
import com.gateway.patterns.singleton.AppConfigManager;

public class PaymentGatewayProxy implements PaymentGateway {

    private final PaymentGateway gateway;
    private final AppConfigManager config;
    private final AtomicInteger requestsInWindow = new AtomicInteger();
    private volatile long windowStartMillis = Instant.now().toEpochMilli();

    public PaymentGatewayProxy(PaymentGateway gateway, AppConfigManager config) {
        if (gateway == null || config == null) {
            throw new IllegalArgumentException("gateway and config must not be null");
        }
        this.gateway = gateway;
        this.config = config;
    }

    @Override
    public String getProvider() {
        return gateway.getProvider();
    }

    @Override
    public void executePayment(double amount) {
        checkRateLimit();
        long startNanos = System.nanoTime();
        gateway.executePayment(amount);
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        if (elapsedMillis > config.getApiTimeoutMillis()) {
            throw new IllegalStateException("Payment API timeout exceeded");
        }
    }

    private synchronized void checkRateLimit() {
        long now = Instant.now().toEpochMilli();
        if (now - windowStartMillis >= 60_000L) {
            windowStartMillis = now;
            requestsInWindow.set(0);
        }
        if (requestsInWindow.incrementAndGet() > config.getRateLimitPerMinute()) {
            throw new IllegalStateException("Payment rate limit exceeded");
        }
    }
}
