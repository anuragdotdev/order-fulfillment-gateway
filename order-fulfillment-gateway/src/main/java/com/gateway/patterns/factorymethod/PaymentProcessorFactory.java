package com.gateway.patterns.factorymethod;

public interface PaymentProcessorFactory {

	PaymentGateway createGateway();
}
