package com.example.ecommerce.config;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.ecommerce.repository.OfferRepository offerRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            User customer = new User();
            customer.setUsername("customer");
            customer.setPassword("customer123");
            customer.setRole("CUSTOMER");
            userRepository.save(customer);
        }

        if (productRepository.count() == 0) {
            Product p1 = new Product();
            p1.setName("Quantum Gaming Laptop");
            p1.setDescription("Next-gen gaming laptop with neural processing unit and holographic display capabilities.");
            p1.setPrice(450000.00);
            p1.setImageUrl("https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=600&q=80");
            
            Product p2 = new Product();
            p2.setName("Neural VR Headset");
            p2.setDescription("Fully immersive virtual reality headset with direct neural interface for hyper-realistic gaming.");
            p2.setPrice(120000.00);
            p2.setImageUrl("https://images.unsplash.com/photo-1622979135225-d2ba269cf1ac?auto=format&fit=crop&w=600&q=80");
            
            Product p3 = new Product();
            p3.setName("Cybernetic Smartwatch");
            p3.setDescription("Advanced biometric tracking with AI-assisted daily planning and holographic projections.");
            p3.setPrice(35000.00);
            p3.setImageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80");

            Product p4 = new Product();
            p4.setName("Drone Delivery System");
            p4.setDescription("Personal automated drone for instant neighborhood deliveries with 4K camera payload.");
            p4.setPrice(250000.00);
            p4.setImageUrl("https://images.unsplash.com/photo-1508614589041-895b88991e3e?auto=format&fit=crop&w=600&q=80");

            productRepository.save(p1);
            productRepository.save(p2);
            productRepository.save(p3);
            productRepository.save(p4);
        }

        if (offerRepository.count() == 0) {
            com.example.ecommerce.model.Offer offer = new com.example.ecommerce.model.Offer();
            offer.setTitle("Special Override!");
            offer.setDescription("Upgrade your reality with the Neural VR Headset.");
            offer.setDiscountText("Now 20% OFF standard credits.");
            offer.setImageUrl("https://images.unsplash.com/photo-1622979135225-d2ba269cf1ac?auto=format&fit=crop&w=600&q=80");
            offer.setTargetUrl("/products");
            offerRepository.save(offer);
        }
    }
}
