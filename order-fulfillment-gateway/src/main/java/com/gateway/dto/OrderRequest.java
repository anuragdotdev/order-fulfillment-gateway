package com.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
