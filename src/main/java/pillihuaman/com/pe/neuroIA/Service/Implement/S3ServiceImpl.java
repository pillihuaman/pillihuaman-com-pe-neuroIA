package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Service
public class S3ServiceImpl {
    private final S3Client s3Client;
    private final String quotationBucketName; // NUEVO: Campo para el bucket de cotizaciones
    private final S3Presigner s3Presigner;
    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);
    private static final List<String> QUOTATION_FILE_TYPES = List.of("quotation_logo", "quotation_reference");
    private final String assetsBucketName ; // Renombrado para claridad
    public S3ServiceImpl(S3Client s3Client,
                         @Value("${aws.s3.bucket-name}") String assetsBucketName, // Inyecta el bucket de assets
                         @Value("${aws.s3.quotation-bucket-name}") String quotationBucketName, // Inyecta el bucket de cotizaciones
                         S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.assetsBucketName = assetsBucketName; // Asigna el valor inyectado
        this.quotationBucketName = quotationBucketName;
        this.s3Presigner = s3Presigner;
    }

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
        logger.debug("Uploading to quotation bucket: {}", quotationBucketName);
        internalUpload(quotationBucketName, key, inputStream, contentLength, contentType);
    }

    public byte[] downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(assetsBucketName)
                .key(key)
                .build();

        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }

    public void deleteFile(String key) {
        // Intenta eliminar del bucket principal
        if (objectExists(assetsBucketName, key)) {
            logger.info("Archivo con clave '{}' encontrado en el bucket principal '{}'. Procediendo a eliminar.", key, assetsBucketName);
            deleteObjectFromBucket(assetsBucketName, key);
            return; // Termina si el archivo fue encontrado y eliminado
        }

        // Si no se encontró en el principal, intenta en el bucket de cotizaciones
        if (objectExists(quotationBucketName, key)) {
            logger.info("Archivo con clave '{}' no encontrado en el bucket principal. Encontrado en el bucket de cotizaciones '{}'. Procediendo a eliminar.", key, quotationBucketName);
            deleteObjectFromBucket(quotationBucketName, key);
            return;
        }

        logger.warn("El archivo con clave '{}' no fue encontrado en ninguno de los buckets configurados ('{}', '{}'). No se realizó ninguna eliminación.", key, assetsBucketName, quotationBucketName);
    }


    /**
     * Realiza la operación de eliminación en un bucket específico.
     */
    private void deleteObjectFromBucket(String bucket, String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Archivo con clave '{}' eliminado exitosamente del bucket '{}'.", key, bucket);
        } catch (Exception e) {
            logger.error("Error al intentar eliminar el archivo con clave '{}' del bucket '{}'.", key, bucket, e);
            // Dependiendo de la política de la aplicación, podrías querer relanzar la excepción
            // throw new RuntimeException("Error al eliminar el archivo de S3", e);
        }
    }
    private void internalUpload(String targetBucket, String key, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(targetBucket) // Usa el nombre del bucket que se le pasa como parámetro
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
    }

    /**
     * Generate a temporary pre-signed URL for the given S3 object key.
     *
     * @param key      the S3 object key
     * @param duration the validity duration for the URL
     * @return a temporary public URL that will expire after the specified duration
     */
    public String generatePresignedUrl(String key, String typeFile, Duration duration) {
        if (key == null || key.isBlank()) {
            return null;
        }

        // 1. Determinar el bucket correcto basándose en el tipo de archivo
        String targetBucket = getBucketForType(typeFile);

        // 2. ¡VALIDACIÓN CLAVE! Verificar que el objeto existe antes de firmar.
        if (!objectExists(targetBucket, key)) {
            logger.warn("Se intentó firmar una URL para un objeto inexistente. Clave: '{}' en Bucket: '{}'", key, targetBucket);
            return null; // Devuelve null si el archivo no existe
        }

        // 3. Si el objeto existe, proceder a generar la URL pre-firmada
        try {
            logger.debug("Generando URL pre-firmada para la clave '{}' en el bucket '{}'", key, targetBucket);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(duration)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedGetObjectRequest.url().toString();

        } catch (Exception e) {
            logger.error("Error inesperado al generar la URL pre-firmada para la clave '{}' en el bucket '{}'", key, targetBucket, e);
            return null; // Devuelve null si ocurre un error durante la firma
        }
    }
    @Deprecated
    public String generatePresignedUrl(String key, Duration duration) {
        // Esta implementación busca en ambos buckets por compatibilidad, pero es ineficiente.
        if (objectExists(assetsBucketName, key)) {
            return generatePresignedUrl(key, "asset", duration); // Asume un tipo genérico
        } else if (objectExists(quotationBucketName, key)) {
            return generatePresignedUrl(key, "quotation_logo", duration); // Asume un tipo de cotización
        } else {
            logger.warn("(Deprecated) El archivo con clave '{}' no fue encontrado en ninguno de los buckets.", key);
            return null;
        }
    }
    private String getBucketForType(String typeFile) {
        if (typeFile != null && QUOTATION_FILE_TYPES.contains(typeFile.toLowerCase())) {
            return this.quotationBucketName;
        }
        return this.assetsBucketName; // Por defecto, el bucket de assets
    }

    private boolean objectExists(String bucket, String key) {
        try {
            // HeadObject es la forma más eficiente (sin descargar el objeto) de verificar la existencia
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            // Esto es normal y esperado si el objeto no existe.
            return false;
        } catch (Exception e) {
            // Cualquier otro error (ej. Access Denied) debería ser loggeado.
            logger.error("Error al verificar la existencia del objeto con clave '{}' en el bucket '{}'", key, bucket, e);
            return false;
        }
    }

}
