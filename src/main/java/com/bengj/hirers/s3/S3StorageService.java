package com.bengj.hirers.s3;

import com.bengj.hirers.constant.ApplicationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements IS3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @Override
    public String uploadProfilePicture(Long userId, MultipartFile file) {
        validateFile(file, "Profile picture");
        String extension = extractExtension(file.getOriginalFilename());
        String objectKey = ApplicationConstants.PROFILE_PICTURES_PREFIX + userId + "/" + UUID.randomUUID() + extension;
        uploadToS3(objectKey, file);
        return objectKey;
    }

    @Override
    public String uploadResume(Long userId, MultipartFile file) {
        validateFile(file, "Resume");
        String extension = extractExtension(file.getOriginalFilename());
        String objectKey = ApplicationConstants.RESUMES_PREFIX + userId + "/" + UUID.randomUUID() + extension;
        uploadToS3(objectKey, file);
        return objectKey;
    }

    @Override
    public byte[] downloadObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            log.warn("Attempted to download object with null/empty S3 key");
            return null;
        }

        try {
            log.debug("Downloading object from S3 bucket: {}, key: {}", bucketName, objectKey);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (NoSuchKeyException e) {
            log.warn("S3 object not found for key: {} in bucket: {}", objectKey, bucketName);
            return null;
        } catch (S3Exception e) {
            log.error("AWS S3 error while downloading object {}: {}", objectKey, e.awsErrorDetails().errorMessage(), e);
            throw new IllegalStateException("Failed to download file from S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while downloading S3 object {}: {}", objectKey, e.getMessage(), e);
            throw new IllegalStateException("Failed to download file from storage", e);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        try {
            log.info("Deleting S3 object: {} from bucket: {}", objectKey, bucketName);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 object: {}", objectKey);
        } catch (S3Exception e) {
            log.error("AWS S3 error while deleting object {}: {}", objectKey, e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting S3 object {}: {}", objectKey, e.getMessage(), e);
        }
    }

    private void uploadToS3(String objectKey, MultipartFile file) {
        try {
            log.info("Uploading file to S3: {} (size: {} bytes, type: {})",
                    objectKey, file.getSize(), file.getContentType());

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey);

            if (file.getContentType() != null && !file.getContentType().isBlank()) {
                requestBuilder.contentType(file.getContentType());
            }

            s3Client.putObject(requestBuilder.build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded object to S3: {}", objectKey);
        } catch (IOException e) {
            log.error("IO error reading file content for S3 upload {}: {}", objectKey, e.getMessage(), e);
            throw new IllegalStateException("Failed to read file for S3 upload: " + e.getMessage(), e);
        } catch (S3Exception e) {
            log.error("AWS S3 error while uploading object {}: {}", objectKey, e.awsErrorDetails().errorMessage(), e);
            throw new IllegalStateException("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading object to S3 {}: {}", objectKey, e.getMessage(), e);
            throw new IllegalStateException("Failed to upload file to storage", e);
        }
    }

    private void validateFile(MultipartFile file, String fileLabel) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileLabel + " file cannot be empty");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "";
    }
}
