package pillihuaman.com.pe.neuroIA.Service;

import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ReqBase;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.dto.ReqIa;
import pillihuaman.com.pe.neuroIA.dto.RespIa;

import java.io.IOException;
import java.util.List;

public interface IAService {

	RespBase<RespIa> getIAResponse(MyJsonWebToken jwt, ReqBase<ReqIa> request);
	RespBase<RespIa> analyzeImage(MyJsonWebToken jwt, MultipartFile file) throws IOException;

	RespBase<RespIa> getIADeepSeek(MyJsonWebToken jwt, ReqBase<ReqIa> request) throws IOException;
	RespBase<RespIa> analyzeImageOpenIA(MyJsonWebToken jwt, MultipartFile file) throws IOException;
}
