package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Settings;
import com.example.ecommerce.repository.SettingsRepository;
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

    @Autowired
    private javax.sql.DataSource dataSource;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private com.example.ecommerce.repository.UserRepository userRepository;

    @GetMapping("/debug-db")
    @ResponseBody
    public String debugDb(HttpSession session) {
        if (!isAdmin(session)) {
            return "Unauthorized";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>Database Diagnostic Panel</h3>");
        try (java.sql.Connection conn = dataSource.getConnection()) {
            sb.append("<p><strong>JDBC URL:</strong> ").append(conn.getMetaData().getURL()).append("</p>");
            sb.append("<p><strong>Database Product Name:</strong> ").append(conn.getMetaData().getDatabaseProductName()).append("</p>");
            sb.append("<p><strong>Database Product Version:</strong> ").append(conn.getMetaData().getDatabaseProductVersion()).append("</p>");
            sb.append("<p><strong>Username:</strong> ").append(conn.getMetaData().getUserName()).append("</p>");
        } catch (Exception e) {
            sb.append("<p style='color:red;'><strong>Connection Error:</strong> ").append(e.getMessage()).append("</p>");
        }
        sb.append("<h4>Environment Variables (Cleaned):</h4>");
        System.getenv().forEach((k, v) -> {
            if (k.contains("URL") || k.contains("HOST") || k.contains("PORT") || k.contains("DATABASE") || k.contains("USER") || k.contains("DB")) {
                String safeValue = v;
                if (k.contains("PASSWORD") || k.contains("PASS") || (v != null && v.contains(":") && v.contains("@"))) {
                    safeValue = v.replaceAll(":[^@/]+@", ":****@");
                }
                sb.append("<p><strong>").append(k).append(":</strong> ").append(safeValue).append("</p>");
            }
        });
        return sb.toString();
    }

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
            // 1. Total Products Count
            java.util.List<Product> products = productService.getAllProducts();
            int productCount = products.size();
            model.addAttribute("totalProducts", productCount);
            
            // 2. Fetch all orders
            java.util.List<Order> orders = orderRepository.findAll();
            
            // 3. Calculate Revenue & Sales
            double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();
            long totalSales = orders.size();
            
            // 4. Calculate Net Income (revenue from DELIVERED orders)
            double netIncome = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
            
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalSales", totalSales);
            model.addAttribute("netIncome", netIncome);
            
            // 5. User registrations
            long totalUsers = userRepository.count();
            long customerCount = userRepository.findAll().stream()
                .filter(u -> "CUSTOMER".equalsIgnoreCase(u.getRole()))
                .count();
            long adminCount = userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .count();
            
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("customerCount", customerCount);
            model.addAttribute("adminCount", adminCount);
            
            // 6. Low stock products (stock <= 5)
            java.util.List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5)
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("lowStockProducts", lowStockProducts);
            
            // 7. Monthly Revenue Aggregate (past 6 months)
            java.util.Map<String, Double> monthlyRevenue = new java.util.LinkedHashMap<>();
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                java.time.YearMonth ym = java.time.YearMonth.from(today.minusMonths(i));
                String monthName = ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
                monthlyRevenue.put(monthName, 0.0);
            }
            for (Order order : orders) {
                if (order.getCreatedAt() != null) {
                    java.time.YearMonth orderYM = java.time.YearMonth.from(order.getCreatedAt());
                    for (int i = 0; i < 6; i++) {
                        java.time.YearMonth targetYM = java.time.YearMonth.from(today.minusMonths(i));
                        if (orderYM.equals(targetYM)) {
                            String monthName = targetYM.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
                            monthlyRevenue.put(monthName, monthlyRevenue.getOrDefault(monthName, 0.0) + order.getTotalAmount());
                        }
                    }
                }
            }
            model.addAttribute("revenueMonths", new java.util.ArrayList<>(monthlyRevenue.keySet()));
            model.addAttribute("revenueValues", new java.util.ArrayList<>(monthlyRevenue.values()));
            
            // 8. Sector/Category Distribution
            java.util.Map<String, Long> categoryData = products.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    p -> p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                    java.util.stream.Collectors.counting()
                ));
            model.addAttribute("categoryLabels", new java.util.ArrayList<>(categoryData.keySet()));
            model.addAttribute("categoryValues", new java.util.ArrayList<>(categoryData.values()));
            
            System.out.println("DEBUG: Strategic Dashboard data successfully computed.");
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
    public String listOrders(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        model.addAttribute("orders", orderRepository.findAllByOrderByCreatedAtDesc());
        return "admin-orders";
    }

    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        orderRepository.deleteById(id);
        return "redirect:/admin/orders?success=order_deleted";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId, 
                                    @RequestParam String status,
                                    @RequestParam(required = false) String courierName,
                                    @RequestParam(required = false) String trackingNumber,
                                    @RequestParam(required = false) String trackingUrl) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            order.setCourierName(courierName);
            order.setTrackingNumber(trackingNumber);
            
            if (trackingUrl != null && !trackingUrl.trim().isEmpty()) {
                order.setTrackingUrl(trackingUrl.trim());
            } else if (trackingNumber != null && !trackingNumber.trim().isEmpty() && courierName != null && !courierName.trim().isEmpty()) {
                String cleanNum = trackingNumber.trim();
                String cleanCourier = courierName.trim().toLowerCase();
                if (cleanCourier.contains("domex")) {
                    order.setTrackingUrl("https://www.domex.lk/tracking.php?tracking_no=" + cleanNum);
                } else if (cleanCourier.contains("koombiyo")) {
                    order.setTrackingUrl("https://koombiyodelivery.com/track?waybill=" + cleanNum);
                } else if (cleanCourier.contains("pronto")) {
                    order.setTrackingUrl("https://www.prontolanka.lk/");
                } else if (cleanCourier.contains("fardar")) {
                    order.setTrackingUrl("https://fardardomestic.com/");
                } else {
                    order.setTrackingUrl("");
                }
            } else {
                order.setTrackingUrl("");
            }
            
            orderRepository.save(order);
        });
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/create-manual")
    public String createManualOrder(@RequestParam String customerName,
                                    @RequestParam String customerPhone,
                                    @RequestParam String customerWhatsapp,
                                    @RequestParam String address,
                                    @RequestParam String itemsManifest,
                                    @RequestParam Double totalAmount) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerWhatsapp(customerWhatsapp);
        order.setAddress(address);
        order.setItemsManifest(itemsManifest);
        order.setTotalAmount(totalAmount);
        order.setStatus("PROCESSING");
        orderRepository.save(order);
        return "redirect:/admin/orders?success=order_logged";
    }

    // --- SETTINGS PROTOCOL ---
    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        String adminWhatsapp = settingsRepository.findById("admin_whatsapp")
                .map(Settings::getSettingValue)
                .orElse("94769414472");
        model.addAttribute("adminWhatsapp", adminWhatsapp);
        return "admin-settings";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam("whatsappNumber") String whatsappNumber, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/signin";
        
        Settings settings = settingsRepository.findById("admin_whatsapp")
                .orElseGet(() -> {
                    Settings s = new Settings();
                    s.setSettingKey("admin_whatsapp");
                    return s;
                });
        settings.setSettingValue(whatsappNumber.trim());
        settingsRepository.save(settings);
        return "redirect:/admin/settings?success=saved";
    }
}
