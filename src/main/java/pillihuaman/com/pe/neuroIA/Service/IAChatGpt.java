package pillihuaman.com.pe.neuroIA.Service;

import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;

import java.io.IOException;
import java.util.List;

public interface IAChatGpt {

	String generateText(List<String> messages, String userMessage, int maxTokens, double temperature) throws IOException;



}
