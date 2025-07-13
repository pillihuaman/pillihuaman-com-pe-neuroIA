package pillihuaman.com.pe.neuroIA.dto;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String message;
    private String context;
    private List<ConversationMessage> conversationHistory;

    @Data
    public static class ConversationMessage {
        private String author;
        private String text;
    }
}