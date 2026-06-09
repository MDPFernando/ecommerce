package com.example.ecommerce.controller;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/signin"; // Require login
        }

        List<CartItem> cartItems = cartService.listCartItems(user);
        double total = cartService.calculateTotal(user);

        // Build WhatsApp Message String dynamically
        StringBuilder waMessage = new StringBuilder("Hello! I would like to finalize my order:%0A%0A");
        StringBuilder manifest = new StringBuilder();
        for(CartItem item : cartItems) {
            waMessage.append("- ").append(item.getQuantity()).append("x ")
                     .append(item.getProduct().getName())
                     .append(" (Rs. ").append(item.getProduct().getPrice() * item.getQuantity()).append(")%0A");
            
            manifest.append("• ").append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity()).append("\n");
        }
        waMessage.append("%0AGrand Total: Rs. ").append(total);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("waMessage", waMessage.toString());
        model.addAttribute("itemsManifest", manifest.toString());

        return "cart";
    }

    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable("id") Long productId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/signin"; // Require login
        }
        
        Product product = productRepository.findById(productId).orElse(null);
        if(product != null) {
            cartService.addProductToCart(user, product);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeCartItem(@PathVariable("id") Long cartItemId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/signin";
        }
        cartService.removeCartItem(cartItemId);
        return "redirect:/cart";
    }
}
