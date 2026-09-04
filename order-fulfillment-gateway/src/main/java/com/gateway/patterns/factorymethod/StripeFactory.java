package com.gateway.patterns.factorymethod;

public class StripeFactory implements PaymentProcessorFactory {

    @Override
    public PaymentGateway createGateway() {
        return new StripeGateway();
    }

    private static final class StripeGateway implements PaymentGateway {

        @Override
        public String getProvider() {
            return "Stripe";
        }

        @Override
        public void executePayment(double amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than zero");
            }
        }
    }
}
