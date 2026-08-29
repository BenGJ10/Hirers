package com.bengj.hirers.s3;

import org.springframework.web.multipart.MultipartFile;

public interface IS3StorageService {

    /**
     * Uploads a profile picture for a given user to S3 under `profile-pictures/{userId}/{UUID}.{ext}`
     *
     * @param userId The ID of the user
     * @param file   The multipart image file to upload
     * @return The generated S3 object key
     */
    String uploadProfilePicture(Long userId, MultipartFile file);

    /**
     * Uploads a resume document for a given user to S3 under `resumes/{userId}/{UUID}.{ext}`
     *
     * @param userId The ID of the user
     * @param file   The multipart document file to upload
     * @return The generated S3 object key
     */
    String uploadResume(Long userId, MultipartFile file);

    /**
     * Downloads the binary data of an S3 object by key.
     *
     * @param objectKey The S3 object key
     * @return The byte array of the downloaded object
     */
    byte[] downloadObject(String objectKey);

    /**
     * Deletes an S3 object by key.
     *
     * @param objectKey The S3 object key to delete
     */
    void deleteObject(String objectKey);
}
