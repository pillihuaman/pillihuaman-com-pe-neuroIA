package pillihuaman.com.pe.neuroIA.foreing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.neuroIA.dto.ProductDTO;

import java.net.URI;
import java.util.*;

@Service
public class ExternalApiService {

    public static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private final RestTemplate restTemplate;


    @Value("${external-api-support.url}")
    private String securityApiSupportUrl;
    @Value("${deepseek.api.url}")
    private String deepseekApiUrl;
    @Value("${deepseek.api.key}")
    private String deepseekApiKey;
    @Value("${deepseek.api.model}")
    private String model;

    @Autowired
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }




    public String getChatResponse(String prompt) {
        logger.info("Getting chat response for prompt: {}", prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepseekApiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            logger.debug("Sending request to DeepSeek API");
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepseekApiUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            logger.debug("Received response from DeepSeek API");
            return extractContentFromResponse(response.getBody());
        } catch (Exception e) {
            logger.error("Error getting chat response", e);
            return "Error: " + e.getMessage();
        }
    }

    public String describeImage(String base64Image) {
        logger.info("Processing image with DeepSeek Vision");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepseekApiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", "deepseek-vision");

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");

        List<Object> content = new ArrayList<>();
        content.add(Map.of("type", "text", "text", "Describe the content of this image in detail in JSON format"));
        content.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image)
        ));

        message.put("content", content);
        messages.add(message);

        request.put("messages", messages);
        request.put("max_tokens", 2048);

        try {
            logger.debug("Sending image processing request to DeepSeek");

            ResponseEntity<Map> response = restTemplate.exchange(
                    deepseekApiUrl + "/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    Map.class
            );

            logger.debug("Received image processing response");

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    Map<String, Object> firstChoice = ((List<Map<String, Object>>) responseBody.get("choices")).get(0);
                    Map<String, Object> messageResponse = (Map<String, Object>) firstChoice.get("message");
                    String contentResponse = (String) messageResponse.get("content");
                    return cleanJsonResponse(contentResponse);
                }
            }

            logger.warn("Failed to process image. Status: {}", response.getStatusCode());
            return "{\"error\":\"Failed to process image\"}";

        } catch (Exception e) {
            logger.error("Image processing failed", e);
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }



    private String cleanJsonResponse(String rawResponse) {
        try {
            logger.debug("Cleaning JSON response");
            if (rawResponse.contains("```json")) {
                return rawResponse.substring(
                        rawResponse.indexOf("```json") + 7,
                        rawResponse.lastIndexOf("```")
                ).trim();
            } else if (rawResponse.contains("```")) {
                return rawResponse.substring(
                        rawResponse.indexOf("```") + 3,
                        rawResponse.lastIndexOf("```")
                ).trim();
            }
            return rawResponse;
        } catch (Exception e) {
            logger.error("Failed to clean JSON response", e);
            return "{\"error\":\"Failed to parse response\",\"raw_response\":\"" +
                    rawResponse.replace("\"", "\\\"") + "\"}";
        }
    }

    private String extractContentFromResponse(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            logger.warn("No valid content found in response");
            return "No response content found";
        } catch (Exception e) {
            logger.error("Error extracting content from response", e);
            return "Error extracting response content";
        }
    }

    public void checkUsage() {
        try {
            logger.debug("Checking API usage");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    deepseekApiUrl + "/usage",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            logger.info("API Usage: {}", response.getBody());
        } catch (Exception e) {
            logger.error("Failed to check API usage", e);
        }
    }
    public String processReceiptImage(String base64Image) {
        try {
            // 1. Clean and validate the base64 string
            String cleanBase64 = base64Image
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "");

            if (cleanBase64.isEmpty()) {
                return "{\"error\":\"Empty image data\"}";
            }

            // 2. Build the properly structured request
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "deepseek-vision");

            // Build messages array
            List<Map<String, Object>> messages = new ArrayList<>();

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");

            // Build content array
            List<Map<String, Object>> content = new ArrayList<>();
            content.add(new LinkedHashMap<String, Object>() {{
                put("type", "text");
                put("text", "Extrae todos los datos de esta boleta peruana en formato JSON. Incluye RUC, items, precios, IGV y totales.");
            }});

            content.add(new LinkedHashMap<String, Object>() {{
                put("type", "image_url");
                put("image_url", new LinkedHashMap<String, Object>() {{
                    put("url", "data:image/jpeg;base64," + cleanBase64);
                    put("detail", "auto"); // Changed from 'high' to 'auto' for better compatibility
                }});
            }});

            message.put("content", content);
            messages.add(message);
            requestBody.put("messages", messages);

            // Add other parameters
            requestBody.put("max_tokens", 3000); // Reduced from 4096
            requestBody.put("response_format", Map.of("type", "json_object"));

            // 3. Configure headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            // 4. Log the request (for debugging)
            logger.debug("Sending request to DeepSeek Vision API");

            // 5. Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepseekApiUrl + "/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            // 6. Process the response
            return extractAndCleanResponse(response);
        } catch (Exception e) {
            logger.error("Error processing receipt image", e);
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String extractAndCleanResponse(ResponseEntity<Map> response) {
        try {
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return "{\"error\":\"API returned status: " + response.getStatusCode() + "\"}";
            }

            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            // Clean the response if it contains markdown
            if (content.contains("```json")) {
                return content.substring(
                        content.indexOf("```json") + 7,
                        content.lastIndexOf("```")
                ).trim();
            } else if (content.contains("```")) {
                return content.substring(
                        content.indexOf("```") + 3,
                        content.lastIndexOf("```")
                ).trim();
            }
            return content;
        } catch (Exception e) {
            logger.error("Error processing API response", e);
            return "{\"error\":\"Failed to process API response\"}";
        }
    }


    public String analizarImagenConDeepSeek(String base64Image) {
        RestTemplate restTemplate = new RestTemplate();

        // Configurar cabeceras
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepseekApiKey);

        // Contenido de imagen
        Map<String, Object> imageUrl = Map.of(
                "url", "data:image/jpeg;base64," + base64Image
        );

        Map<String, Object> imageContent = Map.of(
                "type", "image_url",
                "image_url", imageUrl
        );

        Map<String, Object> textContent = Map.of(
                "type", "text",
                "text", "Describe esta imagen en detalle"
        );

        List<Object> contentList = List.of(textContent, imageContent);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", contentList
        );

        Map<String, Object> requestBody = Map.of(
                "model", "deepseek-vision",
                "messages", List.of(message),
                "max_tokens", 4096
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.deepseek.com/v1/chat/completions",
                    request,
                    String.class
            );
            return response.getBody(); // Devuelve el JSON como String
        } catch (HttpClientErrorException e) {
            System.err.println("Error en la solicitud: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "Error: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error general al procesar la imagen";
        }
    }
    public String analyzeImage(String base64Image, String prompt) {
        try {
            // Limpiar base64
            String cleanBase64 = base64Image
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "");

            if (cleanBase64.isEmpty()) {
                return "{\"error\":\"Empty image data\"}";
            }

            // Crear mensaje tipo usuario con imagen + texto
            Map<String, Object> imageUrl = Map.of(
                    "url", "data:image/jpeg;base64," + cleanBase64,
                    "detail", "auto"
            );

            Map<String, Object> imageContent = Map.of(
                    "type", "image_url",
                    "image_url", imageUrl
            );

            Map<String, Object> textContent = Map.of(
                    "type", "text",
                    "text", prompt != null ? prompt : "Describe the content of this image in detail"
            );

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", List.of(textContent, imageContent)
            );

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "deepseek-vision");
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.7);

            // Log del JSON antes del envío
            ObjectMapper mapper = new ObjectMapper();
            String requestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
            logger.info("📤 Request JSON enviado a DeepSeek:\n{}", requestJson);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);

            // Ejecutar
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepseekApiUrl + "/chat/completions",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            return processVisionResponse(response);

        } catch (HttpClientErrorException.UnprocessableEntity e) {
            logger.error("🔴 DeepSeek 422 Error - Body: {}", e.getResponseBodyAsString());
            logger.error("Headers: {}", e.getResponseHeaders());
            logger.error("Status Code: {}", e.getStatusCode());
            logger.error("Status Text: {}", e.getStatusText());

            // También logea el JSON en caso de error
            try {
                ObjectMapper mapper = new ObjectMapper();
                String requestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(null);
                logger.error("📤 JSON enviado en el error:\n{}", requestJson);
            } catch (Exception logEx) {
                logger.warn("No se pudo loguear el request JSON", logEx);
            }

            return null;

        } catch (HttpClientErrorException | JsonProcessingException e) {
            logger.error("🔴 HTTP Error - Body: {}", e);
            return null;
        }
    }


            private String processVisionResponse(ResponseEntity<Map> response) {
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return "{\"error\":\"API request failed with status: " + response.getStatusCode() + "\"}";
        }

        try {
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

            if (choices == null || choices.isEmpty()) {
                return "{\"error\":\"No choices in API response\"}";
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            // Clean JSON response if it's wrapped in markdown
            return cleanJsonResponse(content);
        } catch (Exception e) {
            logger.error("Error processing vision response", e);
            return "{\"error\":\"Failed to process API response\"}";
        }
    }


    public List<ProductDTO> searchProductsFromSupport(String query, int limit, String authToken) {
        logger.info("Iniciando búsqueda de productos en el servicio de soporte. Query: '{}', Limit: {}", query, limit);

        // *** PASO 1: CONSTRUIR LA URI DE FORMA SEGURA ***
        // Construimos el objeto URI pero NO lo convertimos a String.
        // El método .build() se encarga de la codificación correcta una sola vez.
        URI uri = UriComponentsBuilder.fromHttpUrl(securityApiSupportUrl)
                .path("/private/v1/support/search-for-ia")
                .queryParam("q", query)
                .queryParam("limit", limit)
                .build()
                .toUri();

        logger.debug("URI construida para la llamada externa: {}", uri);

        try {
            // 2. Preparar la solicitud GET con la cabecera de autorización.
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", authToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // *** PASO 2: REALIZAR LA LLAMADA CON RESTTEMPLATE USANDO EL OBJETO URI ***
            // Al pasarle el objeto URI, RestTemplate no volverá a codificarlo.
            ResponseEntity<ExternalApiResponseDTO<List<ProductDTO>>> response = restTemplate.exchange(
                    uri, // <-- Pasamos el objeto URI, no el String.
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ExternalApiResponseDTO<List<ProductDTO>>>() {}
            );

            // 4. Procesar y devolver la respuesta (sin cambios).
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ExternalApiResponseDTO<List<ProductDTO>> body = response.getBody();
                if (body.getStatus() != null && body.getStatus().isSuccess()) {
                    List<ProductDTO> products = body.getPayload();
                    logger.info("Búsqueda exitosa. Se encontraron {} productos.", products != null ? products.size() : 0);
                    return products != null ? products : Collections.emptyList();
                } else {
                    String errorMessage = body.getStatus() != null ? body.getStatus().getMessage() : "Estado de fallo desconocido";
                    logger.warn("El servicio de soporte respondió con un estado de fallo interno: {}", errorMessage);
                    return Collections.emptyList();
                }
            } else {
                logger.warn("La búsqueda de productos no tuvo éxito. Status HTTP: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (HttpClientErrorException e) {
            logger.error("Error del cliente (4xx) al llamar al servicio de soporte. Status: {}. Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error inesperado al conectar con el servicio de soporte en URL: {}", uri, e);
            return Collections.emptyList();
        }
    }
}