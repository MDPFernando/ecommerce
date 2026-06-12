package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.example.ecommerce.service.CartService cartService;

    @Autowired
    private com.example.ecommerce.repository.ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createOrder(@RequestBody Order order, jakarta.servlet.http.HttpSession session) {
        com.example.ecommerce.model.User user = (com.example.ecommerce.model.User) session.getAttribute("loggedInUser");
        if (user != null) {
            order.setUser(user);
            List<com.example.ecommerce.model.CartItem> cartItems = cartService.listCartItems(user);
            List<Product> productsList = new java.util.ArrayList<>();
            for (com.example.ecommerce.model.CartItem item : cartItems) {
                if (item.getProduct() != null) {
                    productsList.add(item.getProduct());
                }
            }
            order.setProducts(productsList);
        }
        Order savedOrder = orderRepository.save(order);
        
        // Stock management & cart clearing
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

    @GetMapping("/my-orders")
    public String viewMyOrders(jakarta.servlet.http.HttpSession session, Model model) {
        com.example.ecommerce.model.User user = (com.example.ecommerce.model.User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/signin";
        }
        
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("orders", orders);
        
        java.util.Set<Long> reviewedProductIds = new java.util.HashSet<>();
        for (Order o : orders) {
            if (o.getProducts() != null) {
                for (Product p : o.getProducts()) {
                    if (reviewRepository.existsByProductIdAndUserId(p.getId(), user.getId())) {
                        reviewedProductIds.add(p.getId());
                    }
                }
            }
        }
        model.addAttribute("reviewedProductIds", reviewedProductIds);
        
        return "my-orders";
    }

    @PostMapping("/reviews/save")
    public String saveReview(@RequestParam("productId") Long productId,
                             @RequestParam("orderId") Long orderId,
                             @RequestParam("rating") int rating,
                             @RequestParam("comment") String comment,
                             @RequestParam(value = "photo", required = false) MultipartFile photo,
                             jakarta.servlet.http.HttpSession session) {
        com.example.ecommerce.model.User user = (com.example.ecommerce.model.User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/signin";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/orders/my-orders?error=product_not_found";
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);

        if (photo != null && !photo.isEmpty()) {
            try {
                String fileName = StringUtils.cleanPath(photo.getOriginalFilename());
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                String uploadDir = "review-images/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                try (java.io.InputStream inputStream = photo.getInputStream()) {
                    java.nio.file.Path filePath = uploadPath.resolve(uniqueFileName);
                    java.nio.file.Files.copy(inputStream, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    review.setImageUrl("/review-images/" + uniqueFileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reviewRepository.save(review);
        return "redirect:/orders/my-orders?success=review_submitted";
    }
}
