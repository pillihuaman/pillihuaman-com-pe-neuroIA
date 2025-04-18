package pillihuaman.com.pe.neuroIA.Service.Implement;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Service.FileProcessService;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

@Service
public class FileProcessServiceImpl implements FileProcessService {

    @Override
    public RespBase<String> readTextFromImage(MultipartFile file) {
        RespBase<String> response = new RespBase<>();
        File tempFile = null;
        try {
            tempFile = convertMultiPartToFile(file);



            BufferedImage bufferedImage = ImageIO.read(tempFile);
            if (bufferedImage == null) {
                throw new IOException("Formato de imagen no soportado");
            }

// PREPROCESAR para OCR
            BufferedImage improvedImage = preprocessImageForOCR(bufferedImage);

            if (bufferedImage == null) {
                throw new IOException("Formato de imagen no soportado");
            }
            System.out.println("Buscando en: " + System.getenv("TESSDATA_PREFIX"));
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Tesseract\\tessdata\\"); // <- apunta directo a la carpeta donde está el archivo

            tesseract.setLanguage("eng");

            String result = tesseract.doOCR(improvedImage);
            response.setPayload(result);
            response.setStatus(RespBase.Status.builder().success(true).build());
        } catch (IOException | TesseractException e) {
            response.setStatus(RespBase.Status.builder()
                    .success(false)
                    .error(RespBase.Status.Error.builder()
                            .code("OCR_ERROR")
                            .httpCode("500")
                            .messages(Collections.singletonList("Error al leer texto de imagen: " + e.getMessage()))
                            .build())
                    .build());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return response;
    }

    @Override
    public RespBase<String> readTextFromPdf(MultipartFile file) {
        RespBase<String> response = new RespBase<>();
        PDDocument document = null;
        try {
            document = PDDocument.load(file.getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            response.setPayload(text);
            response.setStatus(RespBase.Status.builder().success(true).build());
        } catch (IOException e) {
            response.setStatus(RespBase.Status.builder()
                    .success(false)
                    .error(RespBase.Status.Error.builder()
                            .code("PDF_READ_ERROR")
                            .httpCode("400")
                            .messages(Collections.singletonList("Error al leer texto de PDF: " + e.getMessage()))
                            .build())
                    .build());
        }
        return response;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("temp", null);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
    private BufferedImage preprocessImageForOCR(BufferedImage originalImage) {
        // Escalado al 200%
        int width = originalImage.getWidth() * 2;
        int height = originalImage.getHeight() * 2;

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        // Convertir a blanco y negro
        BufferedImage binarizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = binarizedImage.createGraphics();
        g2.drawImage(scaledImage, 0, 0, null);
        g2.dispose();

        return binarizedImage;
    }


}

