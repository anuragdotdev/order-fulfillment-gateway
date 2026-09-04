package com.gateway.dto;

import com.gateway.model.Order;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderResponse {

	String id;
	String status;
	String shippingLabel;
	String customsInvoice;

	public static OrderResponse from(Order order) {
		return OrderResponse.builder()
				.id(order.getId())
				.status(order.getStatus())
				.shippingLabel(order.getShippingLabel())
				.customsInvoice(order.getCustomsInvoice())
				.build();
	}
}
