package com.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.gateway.model.OrderItem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

	private String id;
	private double amount;
	private int riskScore;
	private boolean inStock;
	private boolean authenticated;
	private boolean international;
	private String paymentProvider;
	private String alertRecipient;
	private String customerName;
	private String customerEmail;
	private String shippingAddress;
	private String deliveryPreference;
	private String notificationChannel;
	private String alertUrgency;
	private List<OrderItem> items;
}
