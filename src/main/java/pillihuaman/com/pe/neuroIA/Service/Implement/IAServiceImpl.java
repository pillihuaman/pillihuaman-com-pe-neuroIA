package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Service.FileProcessService;
import pillihuaman.com.pe.neuroIA.Service.IAService;
import pillihuaman.com.pe.neuroIA.dto.ChatRequest;
import pillihuaman.com.pe.neuroIA.dto.ChatResponse;
import pillihuaman.com.pe.neuroIA.dto.ProductDTO;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;
import pillihuaman.com.pe.neuroIA.foreing.ExternalApiService;
import pillihuaman.com.pe.neuroIA.foreing.ExternalOPenIAService;
import pillihuaman.com.pe.neuroIA.repository.store.dao.IaDAO;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static pillihuaman.com.pe.neuroIA.foreing.ExternalApiService.logger;

@Component
public class IAServiceImpl implements IAService {
    @Autowired
    private IaDAO iaDAO;
    @Autowired
    private ExternalApiService externalApiService;
    @Autowired
    private FileProcessService fileProcessService;
    @Autowired
    private ExternalOPenIAService externalOPenIAService;
    @Autowired
    private S3ServiceImpl s3Service;

    private enum UserIntent {
        NEW_SEARCH,
        FOLLOW_UP_QUESTION,
        GENERAL_FAQ
    }

    @Override
    public RespBase<RespIa> getIAResponse(MyJsonWebToken jwt, ReqBase<ReqIa> request) {
        // Lógica original mantenida
        List<String> messages = new ArrayList<>();
        messages.add("User: " + request.getPayload().getTextIA());
        RespIa responseIa = new RespIa();
        responseIa.setTextIA(null);
        return RespBase.<RespIa>builder()
                .payload(null)
                .status(new RespBase.Status(true, null))
                .build();
    }

    @Override
    public RespBase<RespIa> analyzeImage(MyJsonWebToken jwt, MultipartFile file) throws IOException {
        // Lógica original mantenida
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        RespBase<String> data = fileProcessService.readTextFromImage(file);
        externalApiService.analyzeImage(base64Image, "");
        RespIa responseIa = new RespIa();
        responseIa.setDescriptionIA(null);
        return RespBase.<RespIa>builder()
                .payload(responseIa)
                .status(new RespBase.Status(true, null))
                .build();
    }

    @Override
    public RespBase<RespIa> getIADeepSeek(MyJsonWebToken jwt, ReqBase<ReqIa> request) throws IOException {
        // Lógica original mantenida
        String response = externalApiService.getChatResponse(request.getPayload().getTextIA());
        RespIa responseIa = new RespIa();
        responseIa.setTextIA(response);
        return RespBase.<RespIa>builder()
                .payload(responseIa)
                .status(new RespBase.Status(true, null))
                .build();
    }

    @Override
    public RespBase<RespIa> analyzeImageOpenIA(MyJsonWebToken jwt, MultipartFile file) throws IOException {
        // Lógica original mantenida
        String extension = Optional.ofNullable(file.getContentType())
                .map(ct -> switch (ct) {
                    case "image/jpeg" -> ".jpg";
                    case "image/png" -> ".png";
                    case "image/gif" -> ".gif";
                    case "image/webp" -> ".webp";
                    default -> "";
                }).orElse("");
        String key = UUID.randomUUID().toString() + extension;
        String imageUrl = s3Service.generatePresignedUrl(s3Service.uploadFile(key, file.getInputStream(), file.getSize(), file.getContentType()), Duration.ofMinutes(2));
        String jsonResponse = externalOPenIAService.describeImage(imageUrl, "", "");
        RespIa responseIa = new RespIa();
        responseIa.setDescriptionIA(jsonResponse);
        return RespBase.<RespIa>builder()
                .payload(responseIa)
                .status(new RespBase.Status(true, null))
                .build();
    }

    @Override
    public RespBase<ChatResponse> getChatbotResponse(String token, ChatRequest chatRequest) throws IOException {
        String userMessage = chatRequest.getMessage();
        if (userMessage == null || userMessage.isBlank()) {
            return RespBase.<ChatResponse>builder()
                    .payload(ChatResponse.builder().reply("Por favor, escribe una pregunta.").build())
                    .status(new RespBase.Status(false, null))
                    .build();
        }

        // 1. DETECTAR LA INTENCIÓN DEL USUARIO
        UserIntent intent = detectUserIntent(userMessage, chatRequest.getProductContext());
        List<ProductDTO> productsForPrompt = chatRequest.getProductContext();
        String iaTextResponse;

        // 2. ACTUAR SEGÚN LA INTENCIÓN
        switch (intent) {
            case NEW_SEARCH:
                logger.info("Intención detectada: NUEVA_BUSQUEDA. Consultando servicio de soporte para '{}'", userMessage);
                productsForPrompt = externalApiService.searchProductsFromSupport(userMessage, 5, token);
                iaTextResponse = generateResponseFromProducts(userMessage, productsForPrompt, chatRequest);
                break;
            case FOLLOW_UP_QUESTION:
                logger.info("Intención detectada: PREGUNTA_SEGUIMIENTO. Usando contexto de producto existente.");
                iaTextResponse = generateResponseFromProducts(userMessage, productsForPrompt, chatRequest);
                break;
            case GENERAL_FAQ:
            default:
                logger.info("Intención detectada: PREGUNTA_GENERAL_FAQ. Respondiendo con conocimiento general.");
                iaTextResponse = generateGeneralFAQResponse(userMessage, chatRequest);
                break;
        }

        // 3. CONSTRUIR Y DEVOLVER LA RESPUESTA FINAL
        ChatResponse chatResponse = ChatResponse.builder()
                .reply(iaTextResponse)
                .actions(new ArrayList<>())
                .productContext(productsForPrompt)
                .build();

        return RespBase.<ChatResponse>builder()
                .payload(chatResponse)
                .status(new RespBase.Status(true, null))
                .build();
    }

    private UserIntent detectUserIntent(String userMessage, List<ProductDTO> productContext) {
        String lowerCaseMessage = userMessage.toLowerCase();
        List<String> searchKeywords = Arrays.asList("busca", "tienes", "muéstrame", "quiero ver", "precios de", "modelos de");

        if (productContext == null || productContext.isEmpty() || searchKeywords.stream().anyMatch(lowerCaseMessage::contains)) {
            return UserIntent.NEW_SEARCH;
        }

        List<String> faqKeywords = Arrays.asList("envío", "pago", "tienda", "dirección", "courier", "ayacucho", "métodos");
        if (faqKeywords.stream().anyMatch(lowerCaseMessage::contains)) {
            return UserIntent.GENERAL_FAQ;
        }

        return UserIntent.FOLLOW_UP_QUESTION;
    }

    private String generateResponseFromProducts(String userMessage, List<ProductDTO> products, ChatRequest chatRequest) throws IOException {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Eres un asistente de ventas experto y amigable de 'AlamodaPeru.online'. Tu objetivo es vender y ayudar al cliente a tomar la mejor decisión.\n");

        if (products != null && !products.isEmpty()) {
            promptBuilder.append("INSTRUCCIÓN: Responde la pregunta del usuario basándote ESTRICTAMENTE en la siguiente información de nuestro catálogo. Utiliza la 'Guía de Venta' para dar respuestas persuasivas y útiles. Sé conciso y ve al grano.\n\n");
            promptBuilder.append("--- INFORMACIÓN DEL CATÁLOGO ---\n");
            promptBuilder.append(this.formatProductsForPrompt(products));
            promptBuilder.append("----------------------------------\n\n");
        } else {
            promptBuilder.append("INSTRUCCIÓN: Informa amablemente al usuario que no se encontraron productos que coincidan con su búsqueda. Sugiérele probar con otras palabras clave o describir lo que busca.\n\n");
        }

        appendHistoryAndQuestion(promptBuilder, chatRequest);
        logger.debug("Prompt final (Producto) enviado a la IA:\n{}", promptBuilder.toString());
        return externalApiService.getChatResponse(promptBuilder.toString());
    }

    private String generateGeneralFAQResponse(String userMessage, ChatRequest chatRequest) throws IOException {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Eres un asistente de ventas de 'AlamodaPeru.online'.\n");
        promptBuilder.append("INSTRUCCIÓN: Responde la pregunta del usuario con la siguiente información de la empresa. Sé amable y directo.\n\n");
        promptBuilder.append("--- CONOCIMIENTO GENERAL DE LA EMPRESA ---\n");
        promptBuilder.append("- Envíos: Sí, realizamos envíos a todo el Perú a través de Olva Courier. El costo y el tiempo de entrega varían según el destino.\n");
        promptBuilder.append("- Pagos: Aceptamos Yape, Plin, y transferencias bancarias a BCP e Interbank.\n");
        promptBuilder.append("- Tienda física: No tenemos tienda física, somos una tienda 100% online con base en Ayacucho.\n");
        promptBuilder.append("------------------------------------------\n\n");

        appendHistoryAndQuestion(promptBuilder, chatRequest);
        logger.debug("Prompt final (FAQ) enviado a la IA:\n{}", promptBuilder.toString());
        return externalApiService.getChatResponse(promptBuilder.toString());
    }

    private void appendHistoryAndQuestion(StringBuilder promptBuilder, ChatRequest chatRequest) {
        promptBuilder.append("Historial de la conversación reciente:\n");
        List<ChatRequest.ConversationMessage> history = Optional.ofNullable(chatRequest.getConversationHistory()).orElse(Collections.emptyList());
        history.forEach(msg -> {
            promptBuilder.append("user".equals(msg.getAuthor()) ? "Usuario: " : "Asistente: ");
            promptBuilder.append(msg.getText()).append("\n");
        });
        promptBuilder.append("Usuario: ").append(chatRequest.getMessage()).append("\n");
        promptBuilder.append("Asistente:");
    }

    private String formatProductsForPrompt(List<ProductDTO> products) {
        if (products == null || products.isEmpty()) {
            return "No se encontraron productos relevantes en el catálogo.\n";
        }

        return products.stream()
                .map(p -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("## Producto: ").append(p.getName());
                    if (p.getBrand() != null && !p.getBrand().isBlank()) {
                        sb.append(" (Marca: ").append(p.getBrand()).append(")");
                    }
                    sb.append("\n");

                    // --- Guía de Venta: El corazón de la respuesta ---
                    if (p.getSalesGuide() != null) {
                        ProductDTO.SalesGuideDTO guide = p.getSalesGuide();
                        Optional.ofNullable(guide.getValueProposition()).filter(s -> !s.isBlank()).ifPresent(vp -> sb.append("- Propuesta de Valor: ").append(vp).append("\n"));
                        Optional.ofNullable(guide.getTagline()).filter(s -> !s.isBlank()).ifPresent(t -> sb.append("- Lema: '").append(t).append("'\n"));
                        Optional.ofNullable(guide.getFitAndStyleGuide()).filter(s -> !s.isBlank()).ifPresent(fsg -> sb.append("- Guía de Ajuste y Estilo: ").append(fsg).append("\n"));
                        Optional.ofNullable(guide.getTargetAudience()).filter(list -> !list.isEmpty()).ifPresent(ta -> sb.append("- Ideal para: ").append(String.join(", ", ta)).append("\n"));
                        Optional.ofNullable(guide.getUseCases()).filter(list -> !list.isEmpty()).ifPresent(uc -> sb.append("- Perfecto para: ").append(String.join(", ", uc)).append("\n"));
                        Optional.ofNullable(guide.getKeyBenefits()).filter(list -> !list.isEmpty()).ifPresent(kb -> {
                            sb.append("- Beneficios Clave:\n");
                            kb.forEach(b -> sb.append("  - Característica: ").append(b.getFeature()).append(" -> Beneficio: ").append(b.getBenefit()).append("\n"));
                        });
                    }

                    // --- Descripción y Precio ---
                    Optional.ofNullable(p.getDescription()).filter(s -> !s.isBlank()).ifPresent(d -> sb.append("- Descripción: ").append(d).append("\n"));
                    Optional.ofNullable(p.getPricing()).map(ProductDTO.ProductPricingDTO::getSellingPrice).ifPresent(price -> {
                        String currencySymbol = "USD".equalsIgnoreCase(p.getPricing().getCurrency()) ? "$" : "S/";
                        sb.append("- Precio: ").append(currencySymbol).append(price.toPlainString()).append("\n");
                    });

                    // --- Disponibilidad (Stock por talla/color) ---
                    Optional.ofNullable(p.getFileMetadata()).filter(list -> !list.isEmpty()).ifPresent(fm -> {
                        sb.append("- Disponibilidad:\n");
                        fm.stream().filter(meta -> meta.getSizeStock() != null && !meta.getSizeStock().isEmpty()).forEach(meta -> {
                            String stockPorTalla = meta.getSizeStock().stream()
                                    .map(sizeStock -> String.format("Talla %s (%d unidades)", sizeStock.getSize(), sizeStock.getStock()))
                                    .collect(Collectors.joining(", "));
                            String variantName = Optional.ofNullable(meta.getTypeFile()).orElse(Optional.ofNullable(meta.getPosition()).orElse("Estilo Principal"));
                            sb.append("  - En ").append(variantName).append(": ").append(stockPorTalla).append("\n");
                        });
                    });

                    // --- Detalles Técnicos y Medidas ---
                    Optional.ofNullable(p.getSpecifications()).filter(list -> !list.isEmpty()).ifPresent(specs -> {
                        sb.append("- Especificaciones Adicionales:\n");
                        specs.forEach(specGroup -> {
                            sb.append("  - Grupo '").append(specGroup.getGroupName()).append("':\n");
                            specGroup.getAttributes().forEach(attr -> sb.append("    - ").append(attr.getKey()).append(": ").append(attr.getValue()).append("\n"));
                        });
                    });
                    Optional.ofNullable(p.getMeasurements()).filter(list -> !list.isEmpty()).ifPresent(measures -> {
                        sb.append("- Guía de Tallas (cm):\n");
                        measures.forEach(m -> sb.append(String.format("  - Talla %s: Pecho %.1f, Espalda %.1f, Largo %.1f, Manga %.1f\n",
                                m.getSize(), m.getChestContour(), m.getShoulderWidth(), m.getTotalLength(), m.getSleeveLength())));
                    });

                    // --- Preguntas Frecuentes del Producto ---
                    Optional.ofNullable(p.getSalesGuide()).map(ProductDTO.SalesGuideDTO::getFaq).filter(list -> !list.isEmpty()).ifPresent(faqList -> {
                        sb.append("- Preguntas Frecuentes sobre este producto:\n");
                        faqList.forEach(faq -> sb.append("  - P: ").append(faq.getQuestion()).append("\n    R: ").append(faq.getAnswer()).append("\n"));
                    });

                    sb.append("\n");
                    return sb.toString();
                })
                .collect(Collectors.joining());
    }
}