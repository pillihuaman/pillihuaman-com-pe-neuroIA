package pillihuaman.com.pe.neuroIA.Service;

import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.RespBase;

public interface FileProcessService {
	RespBase<String> readTextFromImage(MultipartFile file);
	RespBase<String> readTextFromPdf(MultipartFile file);
}
