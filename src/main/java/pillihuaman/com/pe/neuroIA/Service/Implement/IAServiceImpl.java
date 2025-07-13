package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Service.FileProcessService;
import pillihuaman.com.pe.neuroIA.Service.IAService;
import pillihuaman.com.pe.neuroIA.dto.*;
import pillihuaman.com.pe.neuroIA.foreing.ExternalApiService;
import pillihuaman.com.pe.neuroIA.foreing.ExternalOPenIAService;
import pillihuaman.com.pe.neuroIA.repository.store.dao.IaDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
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

    // @Autowired
    //   private DeepSeekService deepSeekService;
    @Override
    public RespBase<RespIa> getIAResponse(MyJsonWebToken jwt, ReqBase<ReqIa> request) {

        List<String> messages = new ArrayList<>();
        messages.add("User: " + request.getData().getTextIA());

        String chatResponse;
        // chatResponse = iAChatGpt.generateText(messages, request.getData().getTextIA(), 150, 0.7);

        // Crear objeto de respuesta con la información obtenida
        RespIa responseIa = new RespIa();
        responseIa.setTextIA(null);

        return RespBase.<RespIa>builder()
                .payload(null)
                .status(new RespBase.Status(true, null)) // Estado exitoso
                .build();
    }

    @Override
    public RespBase<RespIa> analyzeImage(MyJsonWebToken jwt, MultipartFile file) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        RespBase<String> data = fileProcessService.readTextFromImage(file);
        externalApiService.analyzeImage(base64Image, "");
        RespIa responseIa = new RespIa();
        responseIa.setDescriptionIA(null);
        // Call OpenAI API via ExternalApiService
        return RespBase.<RespIa>builder()
                .payload(responseIa)
                .status(new RespBase.Status(true, null)) // Success status
                .build();
    }

    @Override
    public RespBase<RespIa> getIADeepSeek(MyJsonWebToken jwt, ReqBase<ReqIa> request) throws IOException {
        // String prompt = "Analiza el archivo llamado: " + file.getOriginalFilename();
        String response = externalApiService.getChatResponse(request.getData().getTextIA());

        RespIa responseIa = new RespIa();
        responseIa.setTextIA(response);

        return RespBase.<RespIa>builder()
                .payload(responseIa)
                .status(new RespBase.Status(true, null)) // Estado exitoso
                .build();
    }

    @Override
    public RespBase<RespIa> analyzeImageOpenIA(MyJsonWebToken jwt, MultipartFile file) throws IOException {
        // Extract Base64 or store file and generate public URL (example assumes a URL)
        String extension = switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ""; // fallback if you want to allow unknowns
        };
        String key = UUID.randomUUID().toString() + extension;

        String imageUrl = s3Service.generatePresignedUrl(s3Service.uploadFile(key, file.getInputStream(), file.getSize(), file.getContentType()), Duration.ofMinutes(2));//"https://cloudinary-marketing-res.cloudinary.com/images/w_1000,c_scale/v1679921049/Image_URL_header/Image_URL_header-png?_i=AA"; // Replace with actual logic
        //  String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String jsonResponse = externalOPenIAService.describeImage(imageUrl, "", ""); // This calls your OpenAI method
        RespIa responseIa = new RespIa();
        responseIa.setDescriptionIA(jsonResponse);

        return RespBase.<RespIa>builder()
                .payload(responseIa)
                .status(new RespBase.Status(true, null))
                .build();
    }
    @Override
    public RespBase<ChatResponse> getChatbotResponse(String token, ChatRequest chatRequest) throws IOException {

        // 1. OBTENER EL MENSAJE ACTUAL DEL USUARIO
        // Ahora es más directo gracias al campo 'message' en ChatRequest.
        String userMessage = chatRequest.getMessage();
        if (userMessage == null || userMessage.isBlank()) {
            return RespBase.<ChatResponse>builder()
                    .payload(ChatResponse.builder().reply("Por favor, escribe una pregunta.").build())
                    .status(new RespBase.Status(false, null))
                    .build();
        }

        // 2. RECUPERACIÓN (Retrieval): Buscar productos basados en el mensaje actual.
        logger.info("Buscando productos en el servicio de soporte con la consulta: '{}'", userMessage);
        List<ProductDTO> foundProducts = externalApiService.searchProductsFromSupport(userMessage, 5,token); // Limitamos a 5 resultados

        // 3. AUMENTACIÓN (Augmentation): Construir el prompt detallado para la IA.
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Eres un asistente virtual experto y amigable para 'AlamodaPeru.online', una tienda de ropa deportiva personalizada.\n");
        promptBuilder.append("Tu objetivo es responder las preguntas del usuario y ayudarlo a encontrar los productos que necesita.\n\n");

        // [LA PARTE CLAVE] Inyectamos la información del catálogo en el prompt.
        if (foundProducts != null && !foundProducts.isEmpty()) {
            promptBuilder.append("--- INFORMACIÓN RELEVANTE DE NUESTRO CATÁLOGO ---\n");
            promptBuilder.append(this.formatProductsForPrompt(foundProducts));
            promptBuilder.append("--------------------------------------------------\n\n");
            promptBuilder.append("INSTRUCCIÓN: Usa la información del catálogo anterior para responder la pregunta del usuario de forma precisa. Si un producto no tiene stock, menciónalo. Si no encuentras un producto exacto, sugiere los más parecidos de la lista.\n\n");
        } else {
            promptBuilder.append("INSTRUCCIÓN: No se encontraron productos específicos para la consulta del usuario en nuestro catálogo. Responde amablemente que no tienes esa información y pregunta si puedes ayudarlo con otra cosa.\n\n");
        }

        promptBuilder.append("Contexto de la página actual del usuario: '").append(chatRequest.getContext()).append("'.\n\n");

        // Añadimos el historial de la conversación.
        promptBuilder.append("Historial de la conversación reciente:\n");
        List<ChatRequest.ConversationMessage> history = chatRequest.getConversationHistory() != null ? chatRequest.getConversationHistory() : Collections.emptyList();

        // Se itera sobre la clase anidada ConversationMessage
        history.forEach(msg -> {
            promptBuilder.append(msg.getAuthor().equals("user") ? "Usuario: " : "Asistente: ");
            promptBuilder.append(msg.getText()).append("\n");
        });

        // Finalmente, añadimos la pregunta actual del usuario al final del historial.
        promptBuilder.append("Usuario: ").append(userMessage).append("\n");

        String detailedPrompt = promptBuilder.toString();
        logger.debug("Prompt final enviado a DeepSeek:\n{}", detailedPrompt);

        // 4. GENERACIÓN (Generation): Llamar a la IA con el prompt "aumentado".
        String iaTextResponse = externalApiService.getChatResponse(detailedPrompt);

        // 5. CONSTRUIR LA RESPUESTA para el frontend.
        ChatResponse chatResponse = ChatResponse.builder()
                .reply(iaTextResponse)
                .actions(new ArrayList<>())
                .build();

        // Lógica opcional para añadir acciones (se mantiene igual)
        if ("cotizacion-sublimado".equals(chatRequest.getContext())) {
            chatResponse.getActions().add(
                    ChatResponse.ChatAction.builder()
                            .label("Ver guía de tallas")
                            .type("FUNCTION")
                            .value("mostrarGuiaTallas")
                            .build()
            );
        }

        // 6. DEVOLVER LA RESPUESTA FINAL
        return RespBase.<ChatResponse>builder()
                .payload(chatResponse)
                .status(new RespBase.Status(true, null))
                .build();
    }

    /**
     * Método auxiliar privado para convertir la lista de productos en un texto
     * legible y claro para que la IA lo pueda procesar.
     */
    private String formatProductsForPrompt(List<ProductDTO> products) {
        if (products == null || products.isEmpty()) {
            return "";
        }
        return products.stream()
                .map(p -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("- Producto: ").append(p.getName());

                    if (p.getBrand() != null && !p.getBrand().isBlank()) {
                        sb.append(" (Marca: ").append(p.getBrand()).append(")");
                    }

                    sb.append(". Descripción: ").append(p.getDescription());

                    // *** LA LÍNEA CLAVE DEL CAMBIO ESTÁ AQUÍ ***
                    // Verificamos si el objeto pricing y el sellingPrice existen
                    if (p.getPricing() != null && p.getPricing().getSellingPrice() != null && p.getPricing().getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                        String currencySymbol = "S/"; // Símbolo por defecto
                        if ("USD".equalsIgnoreCase(p.getPricing().getCurrency())) {
                            currencySymbol = "$";
                        }
                        sb.append(". Precio: ").append(currencySymbol).append(" ").append(p.getPricing().getSellingPrice().toPlainString());
                    } else {
                        sb.append(". Precio: Consultar.");
                    }

                    if (p.getStock() > 0) {
                        sb.append(". Stock disponible: ").append(p.getStock()).append(" unidades.");
                    } else {
                        sb.append(". Stock: Agotado.");
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

}
