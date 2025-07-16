package pillihuaman.com.pe.neuroIA.dto;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String message;
    private String context;
    private List<ConversationMessage> conversationHistory;
    // --- CAMPO CLAVE AÑADIDO ---
    // El cliente debe enviar aquí los productos de la respuesta anterior.
    // Será null en la primera interacción.
    private List<ProductDTO> productContext;
    @Data
    public static class ConversationMessage {
        private String author;
        private String text;
    }
}