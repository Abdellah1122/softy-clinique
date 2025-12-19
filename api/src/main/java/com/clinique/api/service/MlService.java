package com.clinique.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class MlService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ML_SERVICE_URL = "http://localhost:8001"; // Assuming uvicorn runs on 8001

    // --- EXISTING METHODS (Assuming they exist or adding them now) ---

    // --- NEW: Sentiment Analysis ---
    public Map<String, Object> analyzeSentiment(String text) {
        String url = ML_SERVICE_URL + "/predict-sentiment";
        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        try {
            // Use ParameterizedTypeReference or simpler approach for now to avoid
            // complexity imports
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return (Map<String, Object>) response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle gracefully
        }
    }

    // --- NEW: Churn Prediction ---
    public Map<String, Object> predictChurn(int daysSinceLast, int totalVisits, double cancellationRate) {
        String url = ML_SERVICE_URL + "/predict-churn";
        Map<String, Object> request = new HashMap<>();
        request.put("days_since_last_visit", daysSinceLast);
        request.put("total_visits", totalVisits);
        request.put("cancellation_rate", cancellationRate);

        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return (Map<String, Object>) response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
