package pillihuaman.com.pe.neuroIA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedAction {
    private ActionType type;
    private String target; // Puede ser una URL ("/clients/new"), un nombre de microservicio ("neuroia-app"), o un nombre de función ("start_return_process").
    private String displayText; // Texto para mostrar al usuario. Ej: "Sí, te llevo a la página para registrar un nuevo cliente".
    private Map<String, Object> parameters; // Parámetros necesarios para la acción. Ej: {"query": "camisa azul"}
}
