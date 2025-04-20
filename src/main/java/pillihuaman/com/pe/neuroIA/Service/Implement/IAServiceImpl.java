package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Service.FileProcessService;
import pillihuaman.com.pe.neuroIA.Service.IAService;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;
import pillihuaman.com.pe.neuroIA.foreing.ExternalApiService;
import pillihuaman.com.pe.neuroIA.foreing.ExternalOPenIAService;
import pillihuaman.com.pe.neuroIA.repository.store.dao.IaDAO;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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
}
