package com.example.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.repository.OfferRepository;
import com.example.ecommerce.repository.OrderRepository;

@SpringBootTest
class EcommerceApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void contextLoads() {
        System.out.println("=== TEST DATABASE DIAGNOSTIC ===");
        System.out.println("Product count: " + productRepository.count());
        productRepository.findAll().forEach(p -> {
            System.out.println("Product ID: " + p.getId() + " | Name: " + p.getName() + " | Price: " + p.getPrice());
        });

        System.out.println("User count: " + userRepository.count());
        userRepository.findAll().forEach(u -> {
            System.out.println("User ID: " + u.getId() + " | Username: " + u.getUsername() + " | Role: " + u.getRole());
        });

        System.out.println("Offer count: " + offerRepository.count());
        offerRepository.findAll().forEach(o -> {
            System.out.println("Offer ID: " + o.getId() + " | Title: " + o.getTitle());
        });

        System.out.println("Order count: " + orderRepository.count());
        orderRepository.findAll().forEach(o -> {
            System.out.println("Order ID: " + o.getId() + " | Customer: " + o.getCustomerName() + " | Status: " + o.getStatus());
        });
        System.out.println("=================================");
    }

}
