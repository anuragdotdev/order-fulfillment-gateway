package com.gateway.dto;

import java.util.List;

import com.gateway.model.OrderItem;

import lombok.Data;

@Data
public class OrderTemplateCloneRequest {

	private String orderId;
	private Double amount;
	private Integer riskScore;
	private Boolean inStock;
	private Boolean authenticated;
	private Boolean international;
	private String customerName;
	private String customerEmail;
	private String shippingAddress;
	private String paymentMethod;
	private String deliveryPreference;
	private String notificationChannel;
	private String alertUrgency;
	private String paymentProvider;
	private String alertRecipient;
	private List<OrderItem> items;
}
