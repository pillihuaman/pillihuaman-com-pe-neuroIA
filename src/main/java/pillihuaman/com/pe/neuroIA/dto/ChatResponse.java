 package pillihuaman.com.pe.neuroIA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private List<ChatAction> actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatAction {
        private String label;
        private String type; // "ROUTE", "API_CALL", "FUNCTION"
        private String value;
    }
}