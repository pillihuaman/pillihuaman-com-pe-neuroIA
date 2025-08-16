package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import pillihuaman.com.pe.neuroIA.config.CacheConfig;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;

@Service
public class S3ServiceImpl {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String assetsBucketName;
    private final String quotationBucketName;
    private final Cache signedUrlCache; // <-- ¡CORRECCIÓN #2: Usar el tipo de Spring!
    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);

    @Autowired
    public S3ServiceImpl(S3Client s3Client,
                         S3Presigner s3Presigner,
                         @Value("${aws.s3.bucket-name}") String assetsBucketName,
                         @Value("${aws.s3.quotation-bucket-name}") String quotationBucketName,
                         CacheManager cacheManager) { // Spring inyecta el CacheManager @Primary
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.assetsBucketName = assetsBucketName;
        this.quotationBucketName = quotationBucketName;
        this.signedUrlCache = cacheManager.getCache(CacheConfig.SIGNED_URL_CACHE);
    }

    /**
     * Genera una URL pre-firmada, utilizando una caché para reutilizar URLs válidas.
     */
    public String generatePresignedUrl(String key, Duration duration) {
        if (key == null || key.isBlank()) {
            return null;
        }

        // 1. Intenta obtener la URL desde la caché.
        //    Esta llamada ahora es correcta y compilará sin problemas.
        String cachedUrl = signedUrlCache.get(key, String.class);
        if (cachedUrl != null) {
            logger.debug("CACHE HIT: URL para la clave '{}' encontrada en la caché.", key);
            return cachedUrl;
        }

        // 2. Si no está en la caché, genera una nueva URL.
        logger.debug("CACHE MISS: No se encontró URL para la clave '{}'. Generando una nueva.", key);
        try {
            String targetBucket = objectExists(assetsBucketName, key) ? assetsBucketName :
                    objectExists(quotationBucketName, key) ? quotationBucketName : null;

            if (targetBucket == null) {
                logger.warn("El archivo con clave '{}' no fue encontrado en ninguno de los buckets.", key);
                return null;
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(duration)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
            String newUrl = presignedGetObjectRequest.url().toString();

            // 3. Guarda la nueva URL en la caché antes de devolverla.
            signedUrlCache.put(key, newUrl);
            logger.debug("URL para la clave '{}' guardada en la caché.", key);

            return newUrl;

        } catch (Exception e) {
            logger.error("Error inesperado al generar la URL pre-firmada para la clave '{}'", key, e);
            return null;
        }
    }

    // --- Resto de los métodos (upload, delete, etc.) ---

    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(assetsBucketName)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        return key;
    }

    public void uploadFileToQuotationBucket(String key, InputStream inputStream, long contentLength, String contentType) {
        internalUpload(quotationBucketName, key, inputStream, contentLength, contentType);
    }

    private void internalUpload(String targetBucket, String key, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
    }

    public byte[] downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(assetsBucketName)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }

    public void deleteFile(String key) {
        if (objectExists(assetsBucketName, key)) {
            deleteObjectFromBucket(assetsBucketName, key);
        } else if (objectExists(quotationBucketName, key)) {
            deleteObjectFromBucket(quotationBucketName, key);
        } else {
            logger.warn("El archivo con clave '{}' no fue encontrado en ninguno de los buckets.", key);
        }
    }

    private void deleteObjectFromBucket(String bucket, String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            logger.error("Error al eliminar el archivo '{}' del bucket '{}'.", key, bucket, e);
        }
    }

    private boolean objectExists(String bucket, String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar la existencia del objeto con clave '{}' en el bucket '{}'", key, bucket, e);
            return false;
        }
    }
}