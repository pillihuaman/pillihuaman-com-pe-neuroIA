package pillihuaman.com.pe.neuroIA.foreing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ExternalOPenIAService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalOPenIAService.class);
    private final RestTemplate restTemplate;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    public ExternalOPenIAService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String describeImage(String imageUrl, String promptText, String detail) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Base system prompt
            List<Map<String, Object>> taskContent = new ArrayList<>();
           /* taskContent.add(Map.of("type", "text", "text",
                    "Extract all the structured content of this image (only text) and return the result in a clean, hierarchical JSON format. " +
                            "Do not explain. Only return pure structured JSON grouped in collections."
            ));*/

            taskContent.add(Map.of("type", "text", "text",
                    "Analiza detalladamente esta imagen de una factura o boleta. Extrae únicamente los textos visibles\n" +
                            " y organízalos en formato JSON estructurado con los siguientes campos:\n" +
                            "No expliques nada. Devuelve exclusivamente el contenido en JSON válido y bien formateado"
            ));




            // Optional custom prompt
            if (promptText != null && !promptText.isBlank()) {
                taskContent.add(Map.of("type", "text", "text", promptText));
            }

            // Image content (URL or base64)
            Map<String, Object> imageUrlMap = new HashMap<>();
            if (imageUrl.startsWith("data:image")) {
                imageUrlMap.put("url", imageUrl);  // base64
            } else {
                imageUrlMap.put("url", imageUrl);  // normal URL
            }

            if (detail != null && !detail.isBlank()) {
                imageUrlMap.put("detail", detail);
            }

            taskContent.add(Map.of("type", "image_url", "image_url", imageUrlMap));

            // Build messages array
            List<Map<String, Object>> messages = List.of(
                    Map.of("role", "user", "content", taskContent)
            );

            // Final request payload
            Map<String, Object> request = new HashMap<>();
            request.put("model", "gpt-4.1");
            request.put("messages", messages);
            request.put("max_tokens", 1000);

            String jsonBody = mapper.writeValueAsString(request);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // Execute request
            ResponseEntity<String> response = restTemplate.exchange(
                    openaiApiUrl + "/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse OpenAI response
            JsonNode root = mapper.readTree(response.getBody());
            String rawContent = root.path("choices").get(0).path("message").path("content").asText();

            // Strip markdown code block if present
            if (rawContent.startsWith("```json")) {
                rawContent = rawContent.replaceAll("^```json\\s*", "")
                        .replaceAll("```$", "")
                        .trim();
            }

            // Validate and pretty-print JSON output
            try {
                JsonNode structuredJson = mapper.readTree(rawContent);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(structuredJson);
            } catch (JsonProcessingException innerEx) {
                logger.warn("Returned content was not valid JSON. Returning raw text instead.");
                return rawContent;
            }

        } catch (HttpClientErrorException | JsonProcessingException ex) {
            logger.error("Error describing image: {}", ex.getMessage(), ex);
            return "{\"error\":\"" + ex.getMessage().replace("\"", "'") + "\"}";
        }
    }


}
