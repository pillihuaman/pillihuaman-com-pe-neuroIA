package pillihuaman.com.pe.neuroIA.Service.Implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
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
