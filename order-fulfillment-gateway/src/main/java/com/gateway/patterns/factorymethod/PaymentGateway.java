package com.gateway.patterns.factorymethod;

public interface PaymentGateway {

    String getProvider();

    void executePayment(double amount);
}
