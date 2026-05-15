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
        if ("YOUR_GEMINI_API_KEY_HERE".equals(apiKey)) {
            return "SYSTEM ALERT: Gemini API Key missing. Please update the application.properties file with a valid API key to enable my neural network.";
        }

        try {
            // System instruction included in the prompt with real-time inventory
            String systemInstruction = "You are NET, a highly advanced AI assistant for Next Era Technologies. " +
                "You have access to the current LIVE inventory database. " +
                "Current Inventory: " + inventoryContext + "\n\n" +
                "Rules:\n" +
                "1. Only confirm items that are in the Current Inventory list.\n" +
                "2. If an item is NOT in the list, professionally suggest similar categories or check back later.\n" +
                "3. Stay in character: advanced, cybernetic, and professional.\n\n";

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
                return "System Malfunction: Unable to process request at this time.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Connection Error: Core neural link offline. (" + e.getMessage() + ")";
        }
    }
}
