package pillihuaman.com.pe.neuroIA.ds;



import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DeepSeekModels {

    public record Config(String apiKey, String baseUrl) {
        public Config {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("API key cannot be null or blank");
            }
            baseUrl = baseUrl == null || baseUrl.isBlank() ? DeepSeekClient.DEFAULT_BASE_URL : baseUrl;
        }
    }
    public record Message(String role, String content) {}
    public record ChatRequest(String model, List<Message> messages) {}
    public record ChatResponse(List<Choice> choices, Usage usage) {}
    public record Choice(@JsonProperty("message") Message message) {}
    public record Usage(
            @JsonProperty("prompt_tokens") int promptTokens,
            @JsonProperty("completion_tokens") int completionTokens,
            @JsonProperty("total_tokens") int totalTokens
    ) {}

}
