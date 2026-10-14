package com.gateway.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

	private String name;
	private int quantity;
	private double unitPrice;
	private double unitWeightKg;

	public OrderItem copy() {
		return new OrderItem(name, quantity, unitPrice, unitWeightKg);
	}
}
