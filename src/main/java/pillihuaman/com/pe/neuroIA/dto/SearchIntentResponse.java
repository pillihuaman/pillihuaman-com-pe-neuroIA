package pillihuaman.com.pe.neuroIA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pillihuaman.com.pe.neuroIA.Help.IntentType;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIntentResponse {

    // --- CLASIFICACIÓN PRIMARIA ---
    private IntentType primaryIntent; // El nuevo enum más específico.
    private double confidenceScore;   // ¿Qué tan segura está la IA de esta clasificación?

    // --- ENTIDADES EXTRAÍDAS ---
    private String primaryEntity;      // La entidad principal ("producto", "cliente", "pedido").
    private String entityIdentifier;   // NUEVO: El identificador específico si se encuentra. Ej: "12345" para un pedido, "Juan Pérez" para un cliente.
    private String actionVerb;         // El verbo de la acción ("registrar", "ver", "devolver").

    // --- DETALLES PARA BÚSQUEDA DE PRODUCTOS ---
    private Map<String, String> productFilters; // Filtros específicos para productos: {"color": "azul", "talla": "M"}.
    private String refinedProductQuery; // La consulta optimizada para buscar en la base de datos de productos.

    // --- SUGERENCIAS PARA EL ORQUESTADOR ---
    private List<SuggestedAction> suggestedActions; // NUEVO: La IA puede sugerir múltiples acciones.

    // --- METADATOS ---
    private String originalQuery;      // Siempre es bueno devolver la consulta original para trazabilidad.
    private long processingTimeMs;     // NUEVO: ¿Cuánto tardó la IA en analizar? Útil para métricas.

    private Map<String, Object> dynamicQueryPlan;


}

// --- CLASES DE SOPORTE PARA EL DTO ---

