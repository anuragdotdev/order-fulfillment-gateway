package com.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    @Column(name = "authenticated", nullable = false)
    private boolean authenticated;

    @Column(name = "is_international", nullable = false)
    private boolean international;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "shipping_label", columnDefinition = "TEXT")
    private String shippingLabel;

    @Column(name = "customs_invoice", columnDefinition = "TEXT")
    private String customsInvoice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "customer_name", length = 160)
    private String customerName;

    @Column(name = "customer_email", length = 254)
    private String customerEmail;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "payment_method", length = 32)
    private String paymentMethod;

    @Column(name = "delivery_preference", length = 32)
    private String deliveryPreference;

    @Column(name = "shipping_cost")
    private Double shippingCost;

    @Column(name = "estimated_delivery_days")
    private Integer estimatedDeliveryDays;

    @Column(name = "notification_channel", length = 32)
    private String notificationChannel;

    @Column(name = "alert_urgency", length = 32)
    private String alertUrgency;

    public Order copy() {
        Order copy = new Order(id, amount, riskScore, inStock, authenticated, international, status,
                shippingLabel, customsInvoice, createdAt, new ArrayList<>(), customerName, customerEmail,
                shippingAddress, paymentMethod, deliveryPreference, shippingCost, estimatedDeliveryDays,
                notificationChannel, alertUrgency);
        if (items != null) {
            copy.setItems(new ArrayList<>(items.stream().map(OrderItem::copy).toList()));
        }
        return copy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}