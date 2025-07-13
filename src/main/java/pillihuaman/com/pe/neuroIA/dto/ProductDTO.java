package pillihuaman.com.pe.neuroIA.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa la respuesta de un producto desde el microservicio de soporte.
 * Su estructura refleja la del JSON, incluyendo objetos anidados.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {

    // --- Campos básicos ---
    private String name;
    private String description;
    private int stock;
    private String category;
    private String brand;

    // --- Objeto Anidado para el Precio ---
    // El nombre de esta propiedad, "pricing", debe coincidir con el nombre
    // del campo en el JSON de respuesta del servicio de soporte.
    private ProductPricingDTO pricing;

    /**
     * Clase interna que representa el objeto 'pricing' anidado en la respuesta.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductPricingDTO {
        // El nombre "sellingPrice" debe coincidir con el campo dentro del objeto pricing.
        private BigDecimal sellingPrice;
        private String currency;
    }
}