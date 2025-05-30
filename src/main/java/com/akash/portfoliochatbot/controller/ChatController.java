package com.akash.portfoliochatbot.controller;

import com.akash.portfoliochatbot.model.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ChatController {

    private final Profile profile;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public ChatController() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("profile.json");
        profile = mapper.readValue(inputStream, Profile.class);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> requestBody)
            throws IOException, InterruptedException {

        String userQuestion = requestBody.get("message");
        Map<String, Object> response = new HashMap<>();

        if (userQuestion == null || userQuestion.isBlank()) {
            response.put("error", "Missing 'message' field in request");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Convert profile to JSON string
            ObjectMapper mapper = new ObjectMapper();
            String profileJson = mapper.writeValueAsString(profile);

            // Enhanced prompt for structured, concise responses
            String prompt = String.format("""
                You are Akash's personal AI assistant on his portfolio website. 
                Provide concise, well-structured responses suitable for a chat interface.
                
                Guidelines:
                - Keep responses under 150 words
                - Use bullet points or numbered lists when appropriate
                - Be sarcastic and friendly and dark humor is allowed
                - Break long information into digestible chunks
                - Use emojis sparingly but effectively
                - Format responses for easy reading in a chat bubble
                - If the question is funny or humorous, don't hesitate to add some fun to the response'
                
                Akash's profile data:
                %s
                
                User question: "%s"
                
                Provide a helpful, structured response:
                """, profileJson, userQuestion);

            // Build Gemini request with specific parameters for better responses
            String safePrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
            String requestBodyJson = String.format("""
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.7,
                "maxOutputTokens": 200,
                "topP": 0.8,
                "topK": 40
              }
            }
            """, safePrompt);

            // Send Gemini request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> apiResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Extract and format AI response
            Map<String, Object> responseMap = mapper.readValue(apiResponse.body(), Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    String aiResponse = (String) parts.get(0).get("text");

                    // Clean and format the response
                    String formattedResponse = formatChatResponse(aiResponse.trim());

                    response.put("response", formattedResponse);
                    response.put("status", "success");
                    response.put("timestamp", LocalDateTime.now().toString());

                    return ResponseEntity.ok(response);
                }
            }

            // Fallback response if no content found
            response.put("response", "I'm here to help! Ask me about Akash's experience, skills, projects, or contact info. 😊");
            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Chat API Error: " + e.getMessage());
            e.printStackTrace();

            response.put("response", "Sorry, I'm experiencing technical difficulties. Please try again! 🤖");
            response.put("status", "error");
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method to format responses for better chat display
    private String formatChatResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "I'm here to help! Feel free to ask me anything about Akash. 😊";
        }

        // Remove excessive line breaks
        response = response.replaceAll("\n{3,}", "\n\n");

        // Ensure proper spacing around bullet points
        response = response.replaceAll("\\*\\s*", "• ");
        response = response.replaceAll("\\-\\s*", "• ");

        // Clean up numbered lists
        response = response.replaceAll("(\\d+\\.\\s*)", "\n$1");

        // Remove markdown formatting that doesn't work well in chat
        response = response.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        response = response.replaceAll("\\*(.*?)\\*", "$1");

        // Trim excessive whitespace
        response = response.trim();

        // Change from 500/450 to much higher values
        if (response.length() > 2000) {
            response = response.substring(0, 1800) + "...\n\nWant to know more? Just ask! 😊";
        }
         return response;
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Akash Portfolio Chat API is running!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "2.0.0");
        response.put("gemini_configured", geminiApiKey != null && !geminiApiKey.isEmpty());
        return ResponseEntity.ok(response);
    }

    // Test endpoint to verify profile loading
    @GetMapping("/profile")
    public ResponseEntity<Profile> getProfile() {
        return ResponseEntity.ok(profile);
    }

    // Quick test responses for common questions
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Chat API is working! Try asking about:");
        response.put("suggestions", new String[]{
                "What's your experience?",
                "Tell me about your skills",
                "What projects have you worked on?",
                "How can I contact you?",
                "What's your education background?"
        });
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}