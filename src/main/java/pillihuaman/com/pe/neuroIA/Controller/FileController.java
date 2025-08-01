package pillihuaman.com.pe.neuroIA.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
@Slf4j
@RestController
@RequestMapping(path = Constante.BASE_ENDPOINT + Constante.ENDPOINT + "/files")
public class FileController {
    @Autowired
    private S3ServiceImpl s3Service;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
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
        List<ReqFileMetadata> metadataDTOList = objectMapper.readValue(metadataJson, new TypeReference<List<ReqFileMetadata>>() {
        });

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
                    if (metadata == null)
                        throw new RuntimeException("No se encontró metadata con ID: " + metaDTO.getId());

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
                    if (file == null)
                        throw new IllegalArgumentException("El archivo es obligatorio para una nueva inserción de metadatos.");

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
                        .s3Key(metadata.getS3Key())
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
        Bson query = eq("_id", new ObjectId(id));
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
            @RequestParam(value = "productId", required = false) String productId) {

        log.info("➡️ Llamada a /getCatalogImagen con typeImagen='{}' y productId='{}'", typeImagen, productId);

        List<Bson> filters = new ArrayList<>();
        filters.add(eq("typeFile", typeImagen));
        filters.add(eq("status", true));

        Integer limit = null;
        Bson orderBy;

        if (productId != null && !productId.trim().isEmpty()) {
            if (!ObjectId.isValid(productId)) {
                log.warn("❌ productId inválido recibido: '{}'", productId);
                return ResponseEntity.badRequest().body(null);
            }
            log.debug("🔍 Agregando filtro por productId: {}", productId);
            filters.add(eq("productId", new ObjectId(productId)));
            orderBy = Sorts.ascending("order");

        } else {
            log.debug("🔍 Sin productId, usando orden por uploadTimestamp descendente y limitando a 50 resultados");
            orderBy = Sorts.descending("uploadTimestamp");
            limit = 50;
        }

        Bson query = and(filters);
        log.debug("📄 Filtros finales: {}", query.toBsonDocument().toJson());

        List<FileMetadata> files = metadataRepository.findAllByFilter(query, orderBy, limit);

        if (files == null || files.isEmpty()) {
            log.info("📭 No se encontraron archivos para los filtros especificados");
            return ResponseEntity.noContent().build();
        }

        log.info("📦 Se encontraron {} archivos", files.size());

        Duration duration = Duration.ofMinutes(Constante.LIFE_TIME_IMG_AWS);
        List<RespFileMetadata> response = files.stream().map(file -> {
            String presignedUrl = s3Service.generatePresignedUrl(file.getS3Key(), duration);
            log.debug("🖼️ Archivo: id={}, filename={}, url={}", file.getId(), file.getFilename(), presignedUrl);
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
                    .productId(file.getProductId() != null ? file.getProductId().toString() : null)
                    .build();
        }).collect(Collectors.toList());

        log.info("✅ Respuesta generada con {} elementos", response.size());
        return ResponseEntity.ok(response);
    }

        /**
         * =========================================================================================
         * NUEVA API: Generar URL Pre-Firmada para un Archivo
         * =========================================================================================
         * Este endpoint permite a otros microservicios obtener una URL de acceso temporal
         * a un archivo sin necesidad de credenciales de AWS.
         * <p>
         * CÓMO USARLA:
         * GET /private/v1/ia/files/generate-presigned-url?key=MI_CLAVE_S3&typeFile=MI_TIPO_DE_ARCHIVO
         *
         * @param key               La clave (key/UUID) del objeto en S3.
         * @param typeFile          El tipo de archivo (ej: "quotation_logo", "product_image") para determinar el bucket.
         * @param durationInMinutes La duración opcional de la validez de la URL en minutos (por defecto 30).
         * @return Una respuesta JSON con la URL generada.
         */
        @GetMapping(path = "/generate-presigned-url", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> generatePresignedUrl(
                @RequestParam String key,
                @RequestParam String typeFile,
                @RequestParam(required = false, defaultValue = "30") long durationInMinutes) {
            logger.info("🔐 [generatePresignedUrl] Solicitud recibida. key={}, typeFile={}, duration={} min", key, typeFile, durationInMinutes);

            try {
                jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
            } catch (Exception e) {
                logger.warn("⚠️ [generatePresignedUrl] Token inválido o ausente.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token inválido o ausente."));
            }

            if (key == null || key.isBlank() || typeFile == null || typeFile.isBlank()) {
                logger.warn("❌ [generatePresignedUrl] Parámetros inválidos. key={}, typeFile={}", key, typeFile);
                return ResponseEntity.badRequest().body(Map.of("error", "Los parámetros 'key' y 'typeFile' son obligatorios."));
            }

            try {
                Duration duration = Duration.ofMinutes(durationInMinutes);
                logger.info("🕐 [generatePresignedUrl] Generando URL con duración de {} segundos", duration.toSeconds());

                String url = s3Service.generatePresignedUrl(key, duration);

                if (url == null) {
                    logger.warn("❌ [generatePresignedUrl] No se pudo generar la URL para key: {}", key);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "No se pudo generar la URL. Verifique que la clave y el tipo de archivo sean correctos."));
                }

                logger.info("✅ [generatePresignedUrl] URL generada con éxito: {}", url);

                Map<String, Object> response = new HashMap<>();
                response.put("url", url);
                response.put("key", key);
                response.put("expiresInSeconds", duration.toSeconds());

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("🔥 [generatePresignedUrl] Error inesperado al generar la URL para key: {}", key, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error interno del servidor.", "message", e.getMessage()));
            }
        }



}