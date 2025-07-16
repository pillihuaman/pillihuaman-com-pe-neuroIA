package pillihuaman.com.pe.neuroIA.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO homologado que representa la respuesta completa de un producto desde el microservicio de soporte.
 * Su estructura es un espejo de RespProduct para asegurar una serialización y deserialización sin pérdidas.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {

    // --- Campos de Identificación ---
    private String id;
    private String name;
    private String productCode;
    private String sku;
    private String upc;
    private String barcode;

    // --- Información Básica y de Clasificación ---
    private String description;
    private String category;
    private String subcategory;
    private List<String> tags;
    private Boolean status;

    // --- Proveedor y Fabricante ---
    private String supplierId;
    private String supplierName;
    private String manufacturer;
    private String brand;

    // --- Fechas de Producción ---
    private String expirationDate;
    private String manufacturingDate;

    // ==========================================================
    // --- OBJETOS ANIDADOS QUE REFLEJAN LA ESTRUCTURA COMPLETA ---
    // ==========================================================

    private ProductPricingDTO pricing;
    private ProductInventoryDTO inventory;
    private ProductMediaDTO media;
    private List<FileMetadataDTO> fileMetadata;
    private List<ProductMeasurementDTO> measurements;
    private List<SpecificationGroupDTO> specifications;
    private SalesGuideDTO salesGuide;


    // ====================================================================
    // --- DEFINICIONES DE CLASES DTO ANIDADAS ---
    // ====================================================================

    /**
     * DTO para el objeto 'pricing'.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductPricingDTO {
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private BigDecimal discount;
        private String currency;
    }

    /**
     * DTO para el objeto 'inventory'.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductInventoryDTO {
        private String unitMeasure;
        private Integer minStock;
        private Integer maxStock;
        private Boolean isFeatured;
        private Boolean isNewArrival;
        private String batch;
        private Double weight;
        private Double height;
        private Double width;
        private Double length;
    }

    /**
     * DTO para el objeto 'media'.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductMediaDTO {
        private List<String> imageUrls;
        private String thumbnailUrl;
        private String seoTitle;
        private String seoDescription;
    }

    /**
     * DTO para los metadatos de un archivo (ej. una imagen de un color específico).
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileMetadataDTO {
        private String url;
        private String typeFile;
        private String position;
        private Integer order;
        private List<SizeStockDTO> sizeStock;
    }

    /**
     * DTO que representa el stock para una talla específica.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SizeStockDTO {
        private String size;
        private Integer stock;
    }

    /**
     * DTO para la tabla de medidas de una prenda.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductMeasurementDTO {
        private String size;
        private Double chestContour;
        private Double shoulderWidth;
        private Double totalLength;
        private Double sleeveLength;
    }

    /**
     * DTO para un grupo de especificaciones genéricas.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpecificationGroupDTO {
        private String groupName;
        private List<SpecificationAttributeDTO> attributes;
    }

    /**
     * DTO para un par atributo-valor.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpecificationAttributeDTO {
        private String key;
        private String value;
    }

    /**
     * DTO para la guía de ventas que alimentará a la IA.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SalesGuideDTO {
        private String valueProposition;
        private String tagline;
        private List<String> targetAudience;
        private List<String> useCases;
        private List<BenefitDTO> keyBenefits;
        private String fitAndStyleGuide;
        private List<String> careInstructions;
        private List<FaqItemDTO> faq;
    }

    /**
     * DTO para un par Característica -> Beneficio.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BenefitDTO {
        private String feature;
        private String benefit;
    }

    /**
     * DTO para un item de Preguntas Frecuentes.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FaqItemDTO {
        private String question;
        private String answer;
    }
}