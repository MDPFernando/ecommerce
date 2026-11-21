package com.example.ecommerce.controller;

import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.client.id:974329213397-s8dcis01flqks7mts448h3r6s1f461eh.apps.googleusercontent.com}")
    private String googleClientId;

    @GetMapping("/signin")
    public String viewSignInPage(Model model) {
        model.addAttribute("googleClientId", googleClientId);
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
            model.addAttribute("googleClientId", googleClientId);
            return "signin";
        } catch (Exception e) {
            System.out.println("DEBUG: CRITICAL AUTH ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "System error during authentication: " + e.getMessage());
            model.addAttribute("googleClientId", googleClientId);
            return "signin";
        }
    }

    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        model.addAttribute("googleClientId", googleClientId);
        return "register";
    }

    @PostMapping("/register")
    public String performRegister(@RequestParam String username, @RequestParam String password, Model model) {
        User user = userService.registerUser(username, password, "CUSTOMER");
        if (user != null) {
            return "redirect:/signin?registered";
        }
        model.addAttribute("error", "Username already exists");
        model.addAttribute("googleClientId", googleClientId);
        return "register";
    }

    @PostMapping("/api/auth/google")
    @ResponseBody
    public Map<String, Object> googleAuth(@RequestBody Map<String, String> payload, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String credential = payload.get("credential");
            if (credential == null || credential.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing Google credential token");
                return response;
            }

            // Google ID token is a standard JWT: header.payload.signature
            String[] parts = credential.split("\\.");
            if (parts.length < 2) {
                response.put("success", false);
                response.put("message", "Malformed Google credential token");
                return response;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(payloadJson);

            String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email not provided by Google account");
                return response;
            }

            // Find existing user or auto-register customer
            User user = userRepository.findByUsername(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(email);
                // Assign a secure random password for OAuth accounts
                newUser.setPassword(UUID.randomUUID().toString());
                newUser.setRole("CUSTOMER");
                return userRepository.save(newUser);
            });

            session.setAttribute("loggedInUser", user);
            System.out.println("Google OAuth authenticated user: " + email + " [Role: " + user.getRole() + "]");

            response.put("success", true);
            response.put("redirect", "ADMIN".equals(user.getRole()) ? "/admin/dashboard" : "/");
            return response;
        } catch (Exception e) {
            System.err.println("Google Auth exception: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Google verification failed: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
