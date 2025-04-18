package pillihuaman.com.pe.neuroIA.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Help.Constantes;
import pillihuaman.com.pe.neuroIA.JwtService;
import pillihuaman.com.pe.neuroIA.Service.FileProcessService;
import pillihuaman.com.pe.neuroIA.Service.IAService;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Constantes.BASE_ENDPOINT + Constantes.ENDPOINT + "/iaService")
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
        reqBase.setData(request);
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        return ResponseEntity.ok(iAService.getIAResponse(token, reqBase));
    }

    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespBase<RespIa>> analyzeImage(@RequestParam("file") MultipartFile file) throws IOException {
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        return ResponseEntity.ok(iAService.analyzeImageOpenIA(token, file));
    }

    @PostMapping(value = "/deepseek-chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespBase<RespIa>> askDeepSeek(@RequestBody Map<String, String> requestBody) throws IOException {
        String prompt = requestBody.get("prompt");
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));

        // Crear un ReqIa con el prompt recibido
        ReqIa reqIa = new ReqIa();
        reqIa.setTextIA(prompt);

        // Crear el objeto ReqBase
        ReqBase<ReqIa> reqBase = new ReqBase<>();
        reqBase.setData(reqIa);

        // Llamar al servicio
        RespBase<RespIa> result = iAService.getIADeepSeek(token, reqBase);  // Pasa ReqBase<ReqIa> en lugar de String

        return ResponseEntity.ok(result);
    }


}
