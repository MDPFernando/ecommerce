package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerPhone;
    private String customerWhatsapp;
    private String address;
    
    @Column(columnDefinition = "TEXT")
    private String itemsManifest;
    
    private Double totalAmount;
    
    private String status; // PROCESSING, PACKED, DISPATCHED, OUT_FOR_DELIVERY, DELIVERED
    
    private String courierName;
    private String trackingNumber;
    private String trackingUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "order_products",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new java.util.ArrayList<>();
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PROCESSING";
        }
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "Date Unlogged";
        }
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
