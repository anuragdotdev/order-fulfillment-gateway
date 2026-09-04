package com.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}