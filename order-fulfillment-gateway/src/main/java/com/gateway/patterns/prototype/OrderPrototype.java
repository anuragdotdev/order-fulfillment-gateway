package com.gateway.patterns.prototype;

import com.gateway.model.Order;

public interface OrderPrototype {

	Order cloneForNewOrder(String newOrderId);
}
