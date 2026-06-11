package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.example.ecommerce.service.CartService cartService;

    @Autowired
    private com.example.ecommerce.repository.ProductRepository productRepository;

    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createOrder(@RequestBody Order order, jakarta.servlet.http.HttpSession session) {
        Order savedOrder = orderRepository.save(order);
        
        // Stock management & cart clearing
        com.example.ecommerce.model.User user = (com.example.ecommerce.model.User) session.getAttribute("loggedInUser");
        if (user != null) {
            List<com.example.ecommerce.model.CartItem> cartItems = cartService.listCartItems(user);
            for (com.example.ecommerce.model.CartItem item : cartItems) {
                Product product = item.getProduct();
                if (product != null) {
                    int qty = item.getQuantity();
                    int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    product.setStockQuantity(Math.max(0, currentStock - qty));
                    productRepository.save(product);
                }
            }
            cartService.clearCart(user);
        }
        
        return Map.of("success", true, "orderId", savedOrder.getId());
    }

    @GetMapping("/track")
    public String viewTrackPage(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            String cleanQuery = query.trim();
            // Remove common prefixes case-insensitively
            if (cleanQuery.toLowerCase().startsWith("order id")) {
                cleanQuery = cleanQuery.substring(8).trim();
            } else if (cleanQuery.toLowerCase().startsWith("order")) {
                cleanQuery = cleanQuery.substring(5).trim();
            }
            if (cleanQuery.startsWith(":") || cleanQuery.startsWith("-")) {
                cleanQuery = cleanQuery.substring(1).trim();
            }
            if (cleanQuery.startsWith("#")) {
                cleanQuery = cleanQuery.substring(1).trim();
            }

            boolean found = false;
            try {
                Long id = Long.parseLong(cleanQuery);
                java.util.Optional<Order> orderOpt = orderRepository.findById(id);
                if (orderOpt.isPresent()) {
                    model.addAttribute("order", orderOpt.get());
                    found = true;
                }
            } catch (NumberFormatException e) {
                // Ignore, try search by phone/WhatsApp
            }

            if (!found) {
                List<Order> orders = orderRepository.findByCustomerPhoneOrCustomerWhatsapp(query.trim(), query.trim());
                if (orders.isEmpty() && !cleanQuery.equals(query.trim())) {
                    orders = orderRepository.findByCustomerPhoneOrCustomerWhatsapp(cleanQuery, cleanQuery);
                }
                if (!orders.isEmpty()) {
                    model.addAttribute("orders", orders);
                }
            }
        }
        return "track-order";
    }
}
