package pillihuaman.com.pe.neuroIA.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.JwtService;
import pillihuaman.com.pe.neuroIA.Service.IAService;
import pillihuaman.com.pe.neuroIA.dto.ChatRequest;
import pillihuaman.com.pe.neuroIA.dto.ChatResponse;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;

import java.io.IOException;

@RestController
@RequestMapping(Constante.BASE_ENDPOINT + Constante.ENDPOINT + "/iaService")
public class IAController {

    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private IAService iAService;
    @Autowired
    private JwtService jwtService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespBase<RespIa>> getIa(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pagesize,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String textIa) {
        ReqIa request = new ReqIa();
        request.setId(id);
        request.setDescriptionIA(description);
        request.setTextIA(textIa);
        ReqBase<ReqIa> reqBase = new ReqBase<>();
        reqBase.setPayload(request);
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        return ResponseEntity.ok(iAService.getIAResponse(token, reqBase));
    }

    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespBase<RespIa>> analyzeImage(@RequestParam("file") MultipartFile file) throws IOException {
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        return ResponseEntity.ok(iAService.analyzeImageOpenIA(token, file));
    }

    @PostMapping(value = "/deepseek-chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespBase<ChatResponse>> chatbotInteraction(@RequestBody ChatRequest chatRequest) throws IOException {
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));

        // Llamar al servicio, que ahora se encargará de construir el prompt
        // y devolver el formato ChatResponse.
        RespBase<ChatResponse> result = iAService.getChatbotResponse(httpServletRequest.getHeader("Authorization"), chatRequest);

        return ResponseEntity.ok(result);
    }

}
