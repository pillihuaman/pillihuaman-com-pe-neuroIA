package pillihuaman.com.pe.neuroIA.Service;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.dto.ChatRequest;
import pillihuaman.com.pe.neuroIA.dto.ChatResponse;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;
import pillihuaman.com.pe.neuroIA.dto.SearchIntentResponse;

import java.io.IOException;

public interface IAService {

	RespBase<RespIa> getIAResponse(MyJsonWebToken jwt, ReqBase<ReqIa> request);
	RespBase<RespIa> analyzeImage(MyJsonWebToken jwt, MultipartFile file) throws IOException;

	RespBase<RespIa> getIADeepSeek(MyJsonWebToken jwt, ReqBase<ReqIa> request) throws IOException;
	RespBase<RespIa> analyzeImageOpenIA(MyJsonWebToken jwt, MultipartFile file) throws IOException;
	RespBase<ChatResponse> getChatbotResponse(String token, ChatRequest chatRequest) throws IOException;
	RespBase<SearchIntentResponse> analyzeSearchIntent(MyJsonWebToken jwt, String query) throws JsonProcessingException;

}
