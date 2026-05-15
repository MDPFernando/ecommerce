package com.example.ecommerce.controller;

import com.example.ecommerce.model.User;
import com.example.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signin")
    public String viewSignInPage() {
        return "signin";
    }

    @PostMapping("/signin")
    public String performSignIn(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        System.out.println("DEBUG: Sign-in attempt for user: " + username);
        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                System.out.println("DEBUG: Authentication successful for: " + username);
                session.setAttribute("loggedInUser", user);
                if ("ADMIN".equals(user.getRole())) {
                    System.out.println("DEBUG: Redirecting to Admin Dashboard");
                    return "redirect:/admin/dashboard";
                }
                return "redirect:/";
            }
            System.out.println("DEBUG: Authentication failed - Invalid credentials");
            model.addAttribute("error", "Invalid username or password");
            return "signin";
        } catch (Exception e) {
            System.out.println("DEBUG: CRITICAL AUTH ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "System error during authentication: " + e.getMessage());
            return "signin";
        }
    }

    @GetMapping("/register")
    public String viewRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String performRegister(@RequestParam String username, @RequestParam String password, Model model) {
        User user = userService.registerUser(username, password, "CUSTOMER");
        if (user != null) {
            return "redirect:/signin?registered";
        }
        model.addAttribute("error", "Username already exists");
        return "register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
