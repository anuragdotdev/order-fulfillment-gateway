package com.gateway.patterns.factorymethod;

public class PayPalFactory implements PaymentProcessorFactory {

    @Override
    public PaymentGateway createGateway() {
        return new PayPalGateway();
    }

    private static final class PayPalGateway implements PaymentGateway {

        @Override
        public String getProvider() {
            return "PayPal";
        }

        @Override
        public void executePayment(double amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than zero");
            }
        }
    }
}
