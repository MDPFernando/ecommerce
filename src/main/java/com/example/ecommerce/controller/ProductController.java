package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.example.ecommerce.service.OfferService offerService;

    @Autowired
    private com.example.ecommerce.service.CategoryService categoryService;

    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("activeOffer", offerService.getActiveOffer());
        model.addAttribute("allOffers", offerService.getAllOffers());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }

    @GetMapping("/api/products/search")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<Product> searchProducts(@RequestParam("query") String query) {
        String q = query.toLowerCase().trim();
        return productService.getAllProducts().stream()
            .filter(p -> p.getName().toLowerCase().contains(q) || 
                        (p.getCategory() != null && p.getCategory().getName().toLowerCase().contains(q)))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
    }

    @Autowired
    private com.example.ecommerce.service.GeminiService geminiService;

    @GetMapping("/api/ai/suggest-description")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, String> suggestDescription(@RequestParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return java.util.Map.of("description", "Please enter an Artifact Name first before invoking AI synthesis.");
        }
        
        String suggestion = geminiService.getProductDescriptionSuggestion(name);
        return java.util.Map.of("description", suggestion);
    }

    @GetMapping("/products")
    public String viewProductsPage(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        java.util.List<Product> allProducts = productService.getAllProducts();
        java.util.List<Product> filteredProducts = new java.util.ArrayList<>(allProducts);
        
        if (search != null && !search.trim().isEmpty()) {
            String query = search.toLowerCase().trim();
            filteredProducts = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(query) || 
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(query)) ||
                            (p.getCategory() != null && p.getCategory().getName().toLowerCase().contains(query)))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("searchQuery", search);
        } else if (categoryId != null) {
            filteredProducts = allProducts.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("selectedCategory", categoryId);
        }
        
        // If search results are empty, provide recommendations
        if (filteredProducts.isEmpty() && search != null) {
            java.util.List<Product> recommendations = allProducts.stream()
                .filter(p -> (p.getFeatured() != null && p.getFeatured()))
                .limit(4)
                .collect(java.util.stream.Collectors.toList());
            
            if (recommendations.isEmpty()) {
                recommendations = allProducts.stream().limit(4).collect(java.util.stream.Collectors.toList());
            }
            model.addAttribute("recommendations", recommendations);
        }
        
        model.addAttribute("listProducts", filteredProducts);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }
}
