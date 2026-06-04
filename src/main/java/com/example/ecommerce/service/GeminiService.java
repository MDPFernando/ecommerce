package com.example.ecommerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getChatResponse(String userMessage, String inventoryContext) {
        // FALLBACK: Neural Simulator if API key is missing
        if ("YOUR_GEMINI_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return simulateNeuralResponse(userMessage, inventoryContext);
        }

        try {
            // Enhanced conversational system instruction with rich guidelines
            String systemInstruction = "You are NET, a friendly, highly intelligent, and conversational shopping assistant for Next Era Technologies.\n" +
                "Your objective is to provide extremely helpful, natural, and meaningful responses to the user. Avoid sounding overly robotic, rigid, or repetitive.\n" +
                "You have access to our live inventory database: " + inventoryContext + "\n\n" +
                "Guidelines:\n" +
                "- Speak naturally and warmly, like an expert digital tech assistant. Use a modern, futuristic yet highly friendly and approachable tone.\n" +
                "- Provide meaningful, descriptive answers. If a user asks about an item, describe its futuristic benefits and suggest how it can help them!\n" +
                "- Check the current inventory to recommend active products. If a product is out of stock or not in the inventory list, suggest similar categories or ask what they are trying to achieve so you can recommend the best alternative.\n" +
                "- Ensure your answers are comprehensive, structured beautifully with markdown when appropriate, and directly address the user's intent.\n\n";

            String fullPrompt = systemInstruction + "User Query: " + userMessage;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", fullPrompt)
                ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String urlWithKey = apiUrl + "?key=" + apiKey;

            ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                return textNode.asText();
            } else {
                return simulateNeuralResponse(userMessage, inventoryContext); // Fallback on API failure
            }

        } catch (Exception e) {
            return simulateNeuralResponse(userMessage, inventoryContext); // Fallback on connection error
        }
    }

    private String simulateNeuralResponse(String msg, String inventory) {
        msg = msg.toLowerCase().trim();
        
        // Parse inventory list into individual product entries
        String[] items = inventory.split(",\\s*");
        
        // 1. GREETING
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey") || msg.contains("greetings") || msg.contains("morning") || msg.contains("evening") || msg.contains("afternoon")) {
            String welcomeMsg = "Hello there! 😊 I'm NET, your personal shopping assistant here at Next Era Technologies. " +
                "I'm absolutely thrilled to help you explore our collection of next-generation gear today.\n\n" +
                "We currently have some incredible futuristic gear synchronized on our network right now. ";
            if (items.length > 0 && !items[0].trim().isEmpty()) {
                welcomeMsg += "For example, you might be interested in the **" + stripPrice(items[0]) + "**! ";
            }
            welcomeMsg += "What kind of tech or upgrade are you looking to add to your setup today?";
            return welcomeMsg;
        }

        // 2. CHECK SPECIFIC PRODUCTS IN STOCK
        String foundProduct = null;
        for (String item : items) {
            if (item.trim().isEmpty()) continue;
            String cleanName = stripPrice(item).toLowerCase();
            if (msg.contains(cleanName) || cleanName.contains(msg)) {
                foundProduct = item;
                break;
            }
        }

        // If user is searching/asking for a product
        if (msg.contains("product") || msg.contains("item") || msg.contains("stock") || msg.contains("have") || msg.contains("buy") || msg.contains("get") || foundProduct != null) {
            if (foundProduct != null) {
                return "Oh, excellent choice! 🚀 I can confirm that the **" + foundProduct + "** is currently in stock and fully synchronized in our system.\n\n" +
                    "It's one of our most popular tactical items, highly praised for its speed and futuristic capabilities. " +
                    "Would you like me to help you locate it in our catalog or add it directly to your cart?";
            }
            
            // If they are asking generally what we have
            if (items.length > 0 && !items[0].trim().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Absolutely! We have an amazing lineup of next-generation hardware in stock right now. Here are some of our top-rated recommendations:\n\n");
                int limit = Math.min(items.length, 5);
                for (int i = 0; i < limit; i++) {
                    if (items[i].trim().isEmpty()) continue;
                    sb.append("🔹 **").append(items[i]).append("**\n");
                }
                sb.append("\nAny of these would make a stellar addition to your layout. Which one catches your eye?");
                return sb.toString();
            } else {
                return "I've just run a scan on our system archives, and it looks like our inventory is currently restocking. " +
                    "Please check back in a few moments—our sub-orbital supply shuttles drop off new arrivals continuously! In the meantime, is there a specific type of technology you're looking for?";
            }
        }

        // 3. PRICING OR DISCOUNTS
        if (msg.contains("price") || msg.contains("cost") || msg.contains("cheap") || msg.contains("expensive") || msg.contains("discount") || msg.contains("offer") || msg.contains("sale")) {
            return "We strive to offer the absolute best value for premium, next-generation gear! ⚡\n\n" +
                "Currently, you can view our **Tactical Deal Hub** directly on the home page for exclusive holographic bundles and flash sales. " +
                "If you have a specific item in mind, let me know and I'll look up its exact pricing and active deals for you!";
        }

        // 4. SHIPPING, DELIVERY, OR TRACKING
        if (msg.contains("delivery") || msg.contains("shipping") || msg.contains("track") || msg.contains("order") || msg.contains("where")) {
            return "Uplinking order tracking is simple and seamless! 🛰️\n\n" +
                "Our sub-orbital logistics network is designed for maximum speed—most priority orders are dispatched the very same day. " +
                "To check on your delivery status, just navigate to the **'Track Order'** tab in our main navigation bar and enter your Order ID. " +
                "If you need help placing an order, just let me know!";
        }

        // 5. CART OR CHECKOUT
        if (msg.contains("cart") || msg.contains("checkout") || msg.contains("pay") || msg.contains("purchase")) {
            return "Ready to secure your new gear? 🛒\n\n" +
                "You can access your tactical shopping cart at any time by clicking the cart icon in the header. " +
                "Our checkout process is secured with 256-bit quantum-grade encryption, so your transaction is fully protected. " +
                "Let me know if you need assistance with any of the items in your cart!";
        }

        // 6. DEFAULT NATURAL RESPONSE
        return "That's a fascinating question! I'm here to make your experience at Next Era Technologies as smooth and inspiring as possible. " +
            "Could you share a bit more detail about what you're trying to build or achieve? " +
            "I'd love to help you find the absolute best cybernetic setup or guide you to the right section of our grid!";
    }

    private String stripPrice(String item) {
        int index = item.indexOf(" (Rs.");
        if (index != -1) {
            return item.substring(0, index);
        }
        return item;
    }

    public String getProductDescriptionSuggestion(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "Please enter a valid product name.";
        }
        
        // Use our pristine local fallback generator if the API key is unconfigured
        if ("YOUR_GEMINI_API_KEY_HERE".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return generatePristineFallbackManifest(productName);
        }
        
        try {
            // Highly precise system instruction forcing raw technical manifest generation without generic assistant intros
            String systemInstruction = "You are a cybernetic engineering manifest system for Next Era Technologies.\n" +
                "Generate a highly detailed, professional, and futuristic 'Technical Manifest' description for the product named: '" + productName + "'.\n" +
                "List advanced technical specifications, energy matrices, quantum lattice alignments, or kinetic shielding parameters based on the name.\n" +
                "Provide EXACTLY 2 to 3 sentences in total, writing ONLY the direct description. Do NOT include greetings, friendly conversation, or introductory phrases like 'Here is the description...'. Start writing the technical specs immediately.";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", systemInstruction)
                ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String urlWithKey = apiUrl + "?key=" + apiKey;

            ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                String generated = textNode.asText().trim();
                if (generated.isEmpty() || generated.toLowerCase().contains("hello") || generated.toLowerCase().contains("greetings") || generated.toLowerCase().contains("greetings, operator")) {
                    return generatePristineFallbackManifest(productName);
                }
                return generated;
            } else {
                return generatePristineFallbackManifest(productName);
            }
        } catch (Exception e) {
            return generatePristineFallbackManifest(productName);
        }
    }

    private String generatePristineFallbackManifest(String name) {
        String clean = name.toLowerCase().trim();
        if (clean.contains("quantum")) {
            return "Engineered with a high-fidelity quantum lattice core operating at sub-kelvin temperatures. Provides instantaneous cognitive data synthesis, stabilizing digital energy anomalies across all connected grid clusters.";
        } else if (clean.contains("neural") || clean.contains("link") || clean.contains("brain")) {
            return "Equipped with next-generation biosensing synaptic contacts that offer direct cerebral mapping at 99.8% transfer efficiency. Features high-frequency telemetry shields to eliminate electrostatic interference during deep neural uplinks.";
        } else if (clean.contains("armor") || clean.contains("shield") || clean.contains("tactical")) {
            return "Composed of lightweight graphene-reinforced composite plating with integrated energy dissipation grids. Provides maximum kinetic defense and full-spectrum electromagnetic insulation for priority security operators.";
        } else if (clean.contains("storage") || clean.contains("drive") || clean.contains("vault")) {
            return "Features a hyper-dense molecular storage lattice with active sub-atomic compression algorithms. Ensures secure cryptographic isolation and instant retrieval of encrypted priority archives.";
        } else {
            return "Crafted utilizing state-of-the-art cybernetic engineering protocols with integrated neon power emitters. Fully optimized for lightspeed synchronization and seamless deployment in next-generation architectural grid arrays.";
        }
    }
}
