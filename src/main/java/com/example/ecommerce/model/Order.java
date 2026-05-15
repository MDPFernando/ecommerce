package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PROCESSING";
        }
    }
}
