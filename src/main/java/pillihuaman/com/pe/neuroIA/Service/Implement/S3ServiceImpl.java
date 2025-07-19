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

import java.io.InputStream;
import java.time.Duration;

@Service
public class S3ServiceImpl {
    private final S3Client s3Client;
    private final String bucketName;
    private final String quotationBucketName; // NUEVO: Campo para el bucket de cotizaciones
    private final S3Presigner s3Presigner;
    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);

    public S3ServiceImpl(S3Client s3Client,
                         @Value("${aws.s3.bucket-name}") String bucketName,
                         @Value("${aws.s3.quotation-bucket-name}") String quotationBucketName, // Inyecta el nuevo bucket
                         S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.quotationBucketName = quotationBucketName; // Asigna el valor
        this.s3Presigner = s3Presigner;
    }


    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
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
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }

    public void deleteFile(String key) {
        // Intenta eliminar del bucket principal
        if (objectExists(bucketName, key)) {
            logger.info("Archivo con clave '{}' encontrado en el bucket principal '{}'. Procediendo a eliminar.", key, bucketName);
            deleteObjectFromBucket(bucketName, key);
            return; // Termina si el archivo fue encontrado y eliminado
        }

        // Si no se encontró en el principal, intenta en el bucket de cotizaciones
        if (objectExists(quotationBucketName, key)) {
            logger.info("Archivo con clave '{}' no encontrado en el bucket principal. Encontrado en el bucket de cotizaciones '{}'. Procediendo a eliminar.", key, quotationBucketName);
            deleteObjectFromBucket(quotationBucketName, key);
            return;
        }

        logger.warn("El archivo con clave '{}' no fue encontrado en ninguno de los buckets configurados ('{}', '{}'). No se realizó ninguna eliminación.", key, bucketName, quotationBucketName);
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
            // Esto es esperado si el objeto no existe
            return false;
        }
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
    public String generatePresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(duration)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

}
