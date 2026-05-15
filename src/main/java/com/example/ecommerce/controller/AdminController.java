package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.example.ecommerce.service.OfferService offerService;

    @Autowired
    private com.example.ecommerce.service.CategoryService categoryService;

    @Autowired
    private OrderRepository orderRepository;

    // Helper method to check admin access
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("DEBUG: Entering Admin Dashboard method");
        if (!isAdmin(session)) {
            System.out.println("DEBUG: Admin check failed - Redirecting to signin");
            return "redirect:/signin";
        }
        
        try {
            int productCount = productService.getAllProducts().size();
            model.addAttribute("totalProducts", productCount);
            System.out.println("DEBUG: Dashboard data prepared successfully. Count: " + productCount);
            return "admin-dashboard";
        } catch (Exception e) {
            System.out.println("DEBUG: ERROR IN DASHBOARD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(value = "category", required = false) Long categoryId,
            HttpSession session, 
            Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        java.util.List<Product> products = productService.getAllProducts();
        if (categoryId != null && categoryId > 0) {
            products = products.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("selectedCategory", categoryId);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-products";
    }

    @GetMapping("/products/add")
    public String showAddProductForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, 
                              @RequestParam(value = "imageFile", required = false) MultipartFile multipartFile,
                              HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        try {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                
                String uploadDir = "product-images/";
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(uniqueFileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    product.setImageUrl("/product-images/" + uniqueFileName);
                } catch (IOException ioe) {
                    System.err.println("Error saving file: " + ioe.getMessage());
                    ioe.printStackTrace();
                }
            }
            
            productService.saveProduct(product);
            return "redirect:/admin/products";
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR IN SAVEPRODUCT: " + e.getMessage());
            e.printStackTrace();
            String encodedError = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Unknown error", StandardCharsets.UTF_8);
            return "redirect:/admin/products?error=" + encodedError;
        }
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    // --- OFFERS MANAGEMENT ---

    @GetMapping("/offers")
    public String listOffers(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        model.addAttribute("offers", offerService.getAllOffers());
        return "admin-offers";
    }

    @GetMapping("/offers/add")
    public String showAddOfferForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        model.addAttribute("offer", new com.example.ecommerce.model.Offer());
        model.addAttribute("products", productService.getAllProducts());
        return "admin-offer-form";
    }

    @GetMapping("/offers/edit/{id}")
    public String showEditOfferForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        com.example.ecommerce.model.Offer offer = offerService.getOfferById(id);
        if (offer == null) {
            return "redirect:/admin/offers";
        }
        model.addAttribute("offer", offer);
        model.addAttribute("products", productService.getAllProducts());
        return "admin-offer-form";
    }

    @PostMapping("/offers/save")
    public String saveOffer(@ModelAttribute com.example.ecommerce.model.Offer offer, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        // Transaction-safe deployment is now fully managed by the OfferService
        offerService.saveOffer(offer);
        return "redirect:/admin/offers?success=deployed";
    }

    @GetMapping("/offers/delete/{id}")
    public String deleteOffer(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        offerService.deleteOffer(id);
        return "redirect:/admin/offers";
    }

    // --- CATEGORIES MANAGEMENT ---

    @GetMapping("/categories")
    public String listCategories(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(value = "id", required = false) Long id,
                               @RequestParam("name") String name, 
                               @RequestParam(value = "parentId", required = false) Long parentId,
                               HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        com.example.ecommerce.model.Category category;
        if (id != null) {
            category = categoryService.getCategoryById(id);
        } else {
            category = new com.example.ecommerce.model.Category();
        }
        
        category.setName(name);
        
        if (parentId != null && parentId > 0) {
            category.setParent(categoryService.getCategoryById(parentId));
        } else {
            category.setParent(null);
        }
        
        categoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        com.example.ecommerce.model.Category category = categoryService.getCategoryById(id);
        model.addAttribute("categoryToEdit", category);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    // --- ORDER MANAGEMENT ---
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAllByOrderByCreatedAtDesc());
        return "admin-orders";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
        return "redirect:/admin/orders";
    }
}
