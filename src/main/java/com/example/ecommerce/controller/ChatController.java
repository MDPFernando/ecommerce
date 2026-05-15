package com.example.ecommerce.controller;

import com.example.ecommerce.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private com.example.ecommerce.service.ProductService productService;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Map.of("response", "Please provide a valid query.");
        }
        
        // Fetch current inventory as context
        java.util.List<com.example.ecommerce.model.Product> products = productService.getAllProducts();
        StringBuilder contextBuilder = new StringBuilder();
        for (com.example.ecommerce.model.Product p : products) {
            contextBuilder.append(p.getName()).append(" (Rs. ").append(p.getPrice()).append("), ");
        }
        
        String response = geminiService.getChatResponse(message, contextBuilder.toString());
        return Map.of("response", response);
    }
}
