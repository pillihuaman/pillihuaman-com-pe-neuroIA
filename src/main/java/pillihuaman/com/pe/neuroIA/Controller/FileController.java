package pillihuaman.com.pe.neuroIA.Controller;

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
import java.util.Arrays;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<RespFileMetadata>> uploadFiles(
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestPart("metadata") List<ReqFileMetadata> metadataDTOList,
            @RequestParam("productId") String productId) {

        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));

        List<RespFileMetadata> dtoList = IntStream.range(0, metadataDTOList.size()).mapToObj(i -> {
            ReqFileMetadata metaDTO = metadataDTOList.get(i);
            MultipartFile file = (files != null && i < files.length) ? files[i] : null;

            try {
                String key = null;
                if (file != null) {
                    key = UUID.randomUUID().toString();
                    s3Service.uploadFile(key, file.getInputStream(), file.getSize(), file.getContentType());
                }

                FileMetadata metadata;
                if (metaDTO.getId() != null && !metaDTO.getId().isEmpty()) {
                    // ACTUALIZACIÓN
                    Bson query = eq("_id", new ObjectId(metaDTO.getId()));
                    metadata = metadataRepository.findOneById(query);
                    if (metadata != null) {
                        if (file != null) {
                            metadata.setS3Key(key);
                            metadata.setFilename(file.getOriginalFilename());
                            metadata.setContentType(file.getContentType());
                            metadata.setSize(file.getSize());
                            metadata.setUploadTimestamp(System.currentTimeMillis());
                        }
                        metadata.setDimension(metaDTO.getDimension());
                        metadata.setTypeFile(metaDTO.getTypeFile());
                        metadata.setPosition(metaDTO.getPosition());
                        metadata.setStatus(true);
                        metadataRepository.updateOne(query, metadata);
                    } else {
                        throw new RuntimeException("No se encontró metadata con ID: " + metaDTO.getId());
                    }
                } else {
                    // INSERCIÓN
                    if (file == null) {
                        throw new IllegalArgumentException("El archivo es obligatorio para una nueva inserción.");
                    }

                    metadata = new FileMetadata();
                    metadata.setFilename(file.getOriginalFilename());
                    metadata.setS3Key(key);
                    metadata.setProductId(new ObjectId(productId));
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

                RespFileMetadata dto = new RespFileMetadata();
                dto.setId(metadata.getId().toString());
                dto.setFilename(metadata.getFilename());
                dto.setDimension(metadata.getDimension());
                dto.setPosition(metadata.getPosition());
                //dto.setUrl(s3Service.generatePresignedUrl(metadata.getS3Key(), Duration.ofMinutes(Constante.LIFE_TIME_IMG_AWS)));

                return dto;
            } catch (Exception e) {
                throw new RuntimeException("Error en procesamiento de metadata o archivo en índice " + i, e);
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

    @GetMapping("/getCatalogImagen")
    public ResponseEntity<List<RespFileMetadata>> getCatalogImagen(
            @RequestParam("typeImagen") String typeImagen,
            @RequestParam("productId") String productId) {

        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        String userId = token.getUser().getId().toString();

        Bson query = and(
                eq("userId", userId),
                eq("typeFile", typeImagen),
                eq("status", true),
                eq("productId", new ObjectId(productId))
        );

        List<FileMetadata> files = metadataRepository.findAllByFilter(query);

        if (files == null || files.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Generar DTOs con URL firmada
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
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }



}
