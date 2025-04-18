package pillihuaman.com.pe.neuroIA.foreing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pillihuaman.com.pe.neuroIA.Help.MaestrosUtilidades;

import java.time.Duration;
import java.util.List;
import java.util.Map;
@Service
public class DeepSeekService {
    private static final Logger log = LoggerFactory.getLogger(MaestrosUtilidades.class);

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model}")
    private String model;

    private final RestTemplate restTemplate;

    public DeepSeekService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(30))  // Connection timeout (30s)
                .setReadTimeout(Duration.ofSeconds(60))    // Read timeout (60s)
                .build();
    }

    public String getChatResponse(String prompt) {
        // Clean the URL by removing any fragments/comments
        String cleanApiUrl = apiUrl.split("#")[0].trim();
        String endpoint = cleanApiUrl + "/chat/completions";

        log.info("Using DeepSeek endpoint: {}", endpoint);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                )),
                "temperature", 0.7,
                "max_tokens", 150
        );

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            return extractContent(response.getBody());
        } catch (Exception e) {
            log.error("DeepSeek API call failed", e);
            return "Error: " + e.getMessage();
        }
    }

    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.error("Failed to parse response", e);
            return "Could not parse API response";
        }
    }
}



