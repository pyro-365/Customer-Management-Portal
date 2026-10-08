package com.customer.service;

import com.customer.dto.Requests;
import com.customer.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Only talks to Gemini. Knows nothing about customers or products. */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private final RestClient http;
    private final String apiKey;
    private final String primaryModel;

    public GeminiService(RestClient http, @Value("${gemini.api-key}") String apiKey, @Value("${gemini.model}") String model) {
        this.http = http;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        String m = (model != null && !model.isBlank()) ? model.trim() : "gemini-flash-lite-latest";
        if (m.startsWith("models/")) m = m.substring("models/".length());
        this.primaryModel = m;
    }

    private static Map<String, Object> turn(String role, String text) {
        return Map.of("role", role, "parts", List.of(Map.of("text", text)));
    }

    @SuppressWarnings("null")
    public String ask(String context, List<Requests.Turn> history, String message) {
        if (apiKey.isBlank())
            throw new ApiException(503, "The AI assistant is not set up yet. Add GEMINI_API_KEY and restart the server.");

        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            history.stream()
                    .filter(t -> t.text() != null && !t.text().isBlank())
                    .skip(Math.max(0, history.size() - 6))
                    .forEach(t -> contents.add(turn("model".equals(t.role()) ? "model" : "user", t.text())));
        }
        contents.add(turn("user", message));

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", context))),
                "contents", contents);

        List<String> modelsToTry = new ArrayList<>();
        modelsToTry.add(primaryModel);
        if (!modelsToTry.contains("gemini-flash-lite-latest")) modelsToTry.add("gemini-flash-lite-latest");
        if (!modelsToTry.contains("gemini-3.5-flash-lite")) modelsToTry.add("gemini-3.5-flash-lite");

        ApiException lastException = null;

        for (String m : modelsToTry) {
            try {
                JsonNode res = http.post().uri(URL, m)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body).retrieve().body(JsonNode.class);
                if (res != null) {
                    String text = res.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            } catch (RestClientResponseException e) {
                log.warn("Gemini model '{}' returned status {}: {}", m, e.getStatusCode().value(), e.getResponseBodyAsString());
                lastException = new ApiException(502, "Gemini returned error " + e.getStatusCode().value() + " on " + m + ". Check GEMINI_API_KEY and GEMINI_MODEL.");
            } catch (RestClientException e) {
                log.warn("Could not reach Gemini model '{}': {}", m, e.getMessage());
                lastException = new ApiException(502, "Could not reach Gemini. Check your internet connection.");
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        return "I couldn't come up with an answer. Please rephrase your question.";
    }
}
