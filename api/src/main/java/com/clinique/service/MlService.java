package com.clinique.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class MlService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ML_SERVICE_URL = "http://localhost:8000"; // Adjust port if needed

    // --- EXISTING METHODS (Assuming they exist or adding them now) ---
    // ... cancellation/timing methods ...

    // --- NEW: Sentiment Analysis ---
    public Map<String, Object> analyzeSentiment(String text) {
        String url = ML_SERVICE_URL + "/predict-sentiment";
        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
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
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
