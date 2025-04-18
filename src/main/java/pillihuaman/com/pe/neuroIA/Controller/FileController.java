package pillihuaman.com.pe.neuroIA.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import pillihuaman.com.pe.neuroIA.Service.Implement.S3ServiceImpl;
import pillihuaman.com.pe.neuroIA.repository.files.FileMetadata;
import pillihuaman.com.pe.neuroIA.repository.files.dao.FilesDAO;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private S3ServiceImpl s3Service;

    @Autowired
    private FilesDAO metadataRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                         @RequestParam("dimension") String dimension,
                                         @RequestParam("userId") String userId) {
        if (files.size() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Maximum of 5 files allowed.");
        }

        List<FileMetadata> metadataList = files.stream().map(file -> {
            try {
                String key = UUID.randomUUID().toString();
                String urlName = s3Service.uploadFile(key, file.getInputStream(), file.getSize(), file.getContentType());
                FileMetadata metadata = new FileMetadata();
                metadata.setFilename(file.getOriginalFilename());
                metadata.setS3Key(key);
                metadata.setContentType(file.getContentType());
                metadata.setSize(file.getSize());
                metadata.setHashCode(UUID.randomUUID().toString());
                metadata.setDimension(dimension);
                metadata.setUserId(userId);
                metadata.setUploadTimestamp(System.currentTimeMillis());

                return metadataRepository.save(metadata);
            } catch (Exception e) {
                throw new RuntimeException("Error uploading file: " + file.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());

        return ResponseEntity.ok(metadataList);
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
        Bson query = eq("id", id);
        FileMetadata metadata = metadataRepository.findOneById(query);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }

        metadata.setStatus(false); // Mark as inactive
        metadataRepository.updateOne(query, metadata);
        return ResponseEntity.ok("File marked as inactive successfully.");
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
}
