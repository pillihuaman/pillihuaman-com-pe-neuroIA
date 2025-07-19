package pillihuaman.com.pe.neuroIA.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.JwtService;
import pillihuaman.com.pe.neuroIA.Service.Implement.S3ServiceImpl;
import pillihuaman.com.pe.neuroIA.dto.ReqFileMetadata;
import pillihuaman.com.pe.neuroIA.dto.RespFileMetadata;
import pillihuaman.com.pe.neuroIA.repository.files.FileMetadata;
import pillihuaman.com.pe.neuroIA.repository.files.dao.FilesDAO;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@RestController
@RequestMapping(path = Constante.BASE_ENDPOINT + Constante.ENDPOINT + "/files")
public class FileController {

    @Autowired
    private S3ServiceImpl s3Service;

    @Autowired
    private FilesDAO metadataRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private ObjectMapper objectMapper;
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<List<RespFileMetadata>> uploadFiles(
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestPart("metadata") String metadataJson,
            @RequestParam("productId") String productId) throws JsonProcessingException {

        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        List<ReqFileMetadata> metadataDTOList = objectMapper.readValue(metadataJson, new TypeReference<List<ReqFileMetadata>>() {});

        // Validamos que la cantidad de archivos coincida con la de metadatos si se envían archivos.
        if (files != null && files.length != metadataDTOList.size()) {
            throw new IllegalArgumentException("La cantidad de archivos no coincide con la cantidad de metadatos.");
        }

        List<RespFileMetadata> dtoList = IntStream.range(0, metadataDTOList.size()).mapToObj(i -> {
            ReqFileMetadata metaDTO = metadataDTOList.get(i);
            MultipartFile file = (files != null && i < files.length) ? files[i] : null;

            try {
                String s3Key = null;
                // Sube el archivo a S3 si se proporcionó uno.
                if (file != null && !file.isEmpty()) {
                    s3Key = UUID.randomUUID().toString();
                    String bucketType = metaDTO.getTypeFile();

                    if ("quotation_logo".equalsIgnoreCase(bucketType) || "quotation_reference".equalsIgnoreCase(bucketType)) {
                        s3Service.uploadFileToQuotationBucket(s3Key, file.getInputStream(), file.getSize(), file.getContentType());
                    } else {
                        s3Service.uploadFile(s3Key, file.getInputStream(), file.getSize(), file.getContentType());
                    }
                }

                FileMetadata metadata;

                // Decide si actualizar un metadato existente o crear uno nuevo.
                if (metaDTO.getId() != null && !metaDTO.getId().isEmpty()) {
                    // --- LÓGICA DE ACTUALIZACIÓN ---
                    Bson query = eq("_id", new ObjectId(metaDTO.getId()));
                    metadata = metadataRepository.findOneById(query);
                    if (metadata == null) throw new RuntimeException("No se encontró metadata con ID: " + metaDTO.getId());

                    if (s3Key != null) { // Si se subió un nuevo archivo para reemplazar
                        metadata.setS3Key(s3Key);
                        metadata.setFilename(file.getOriginalFilename());
                        metadata.setContentType(file.getContentType());
                        metadata.setSize(file.getSize());
                    }
                    metadata.setDimension(metaDTO.getDimension());
                    metadata.setTypeFile(metaDTO.getTypeFile());
                    metadata.setPosition(metaDTO.getPosition());
                    metadata.setUploadTimestamp(System.currentTimeMillis());
                    metadata.setStatus(true);
                    metadataRepository.updateOne(query, metadata);
                } else {
                    // --- LÓGICA DE INSERCIÓN ---
                    if (file == null) throw new IllegalArgumentException("El archivo es obligatorio para una nueva inserción de metadatos.");

                    metadata = new FileMetadata();
                    metadata.setFilename(file.getOriginalFilename());
                    metadata.setS3Key(s3Key);
                    if (ObjectId.isValid(productId)) {
                        metadata.setProductId(new ObjectId(productId));
                    }
                    metadata.setContentType(file.getContentType());
                    metadata.setSize(file.getSize());
                    metadata.setHashCode(UUID.randomUUID().toString());
                    metadata.setDimension(metaDTO.getDimension());
                    metadata.setUserId(token.getUser().getId().toString());
                    metadata.setUploadTimestamp(System.currentTimeMillis());
                    metadata.setTypeFile(metaDTO.getTypeFile());
                    metadata.setPosition(metaDTO.getPosition());
                    metadata.setStatus(true);
                    metadata = metadataRepository.save(metadata);
                }

                // --- CONSTRUCCIÓN DE LA RESPUESTA COMPLETA ---
                // Genera la URL pre-firmada para el acceso inmediato al archivo.
                String presignedUrl = (metadata.getS3Key() != null)
                        ? s3Service.generatePresignedUrl(metadata.getS3Key(), Duration.ofMinutes(Constante.LIFE_TIME_IMG_AWS))
                        : null;

                // Mapea la entidad `FileMetadata` (completa) al DTO `RespFileMetadata` (completo).
                return RespFileMetadata.builder()
                        .id(metadata.getId().toString())
                        .filename(metadata.getFilename())
                        .contentType(metadata.getContentType())
                        .size(metadata.getSize())
                        .hashCode(metadata.getHashCode())
                        .dimension(metadata.getDimension())
                        .userId(metadata.getUserId())
                        .uploadTimestamp(metadata.getUploadTimestamp())
                        .status(metadata.isStatus())
                        .order(metadata.getOrder())
                        .typeFile(metadata.getTypeFile())
                        .url(presignedUrl) // Se incluye la URL
                        .position(metadata.getPosition())
                        .productId(metadata.getProductId() != null ? metadata.getProductId().toString() : null)
                        // .sizeStock(mapearSizeStock(metadata.getSizeStock())) // Si necesitas mapear el stock
                        .build();

            } catch (Exception e) {
                throw new RuntimeException("Error procesando el archivo en el índice " + i + ": " + e.getMessage(), e);
            }
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable String id) {
        Bson query = eq("_id", id);
        FileMetadata metadata = metadataRepository.findOneById(query);
        if (metadata == null || !metadata.isStatus()) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = s3Service.downloadFile(metadata.getS3Key());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) {
        Bson query = eq("_id", new ObjectId( id));
        FileMetadata metadata = metadataRepository.findOneById(query);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }

        // Eliminar archivo en S3 si existe clave
        if (metadata.getS3Key() != null && !metadata.getS3Key().isEmpty()) {
            try {
                s3Service.deleteFile(metadata.getS3Key());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error al eliminar archivo en S3: " + e.getMessage());
            }
        }

        metadata.setStatus(false); // Marcar como inactivo
        metadataRepository.deleteFileByQuery(query);
        return ResponseEntity.ok("Archivo marcado como inactivo y eliminado de S3 correctamente.");
    }


    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restoreFile(@PathVariable String id) {
        Bson query = eq("id", id);
        FileMetadata metadata = metadataRepository.findOneById(query);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        metadata.setStatus(true); // Mark as active
        metadataRepository.updateOne(query, metadata);
        return ResponseEntity.ok("File restored successfully.");
    }
// En tu FileController.java

    @GetMapping("/getCatalogImagen")
    public ResponseEntity<List<RespFileMetadata>> getCatalogImagen(
            @RequestParam("typeImagen") String typeImagen,
            @RequestParam(value = "productId", required = false) String productId) { // productId ahora es opcional
        productId="686868af8c3d12409a1f9b04";
        List<Bson> filters = new ArrayList<>();
        filters.add(eq("typeFile", typeImagen));
        filters.add(eq("status", true));

        Integer limit = null;
        Bson orderBy = null;

        if (productId != null && !productId.trim().isEmpty()) {
            if (!ObjectId.isValid(productId)) {
                return ResponseEntity.badRequest().body(null);
            }
            filters.add(eq("productId", new ObjectId(productId)));
            orderBy = Sorts.ascending("order"); // Ordenar por campo 'order' si existe

        } else {
            orderBy = Sorts.descending("uploadTimestamp"); // Ordenar por fecha de subida
            limit = 50; // Limitar a 20 resultados
        }

        Bson query = and(filters);

        // --- LLAMADA AL NUEVO MÉTODO DEL DAO ---
        List<FileMetadata> files = metadataRepository.findAllByFilter(query, orderBy, limit);

        if (files == null || files.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Duration duration = Duration.ofMinutes(Constante.LIFE_TIME_IMG_AWS);
        List<RespFileMetadata> response = files.stream().map(file -> {
            String presignedUrl = s3Service.generatePresignedUrl(file.getS3Key(), duration);
            return RespFileMetadata.builder()
                    .id(file.getId().toString())
                    .filename(file.getFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .hashCode(file.getHashCode())
                    .dimension(file.getDimension())
                    .userId(file.getUserId())
                    .uploadTimestamp(file.getUploadTimestamp())
                    .status(file.isStatus())
                    .order(file.getOrder())
                    .typeFile(file.getTypeFile())
                    .url(presignedUrl)
                    .position(file.getPosition())
                    // Si la imagen de la home está asociada a un producto, también puedes incluir su ID
                    .productId(file.getProductId() != null ? file.getProductId().toString() : null)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }



}