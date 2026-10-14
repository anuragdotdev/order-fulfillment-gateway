package com.gateway.patterns.mediator;

import com.gateway.model.Order;

public interface OrderMediator {

	Order submit(Order order, String paymentProvider, String alertRecipient);
}
