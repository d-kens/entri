package com.entri.modules.file_upload.service;

import com.entri.common.exception.FileUploadException;
import com.entri.common.exception.InvalidFileTypeException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final Bucket bucket;

    public String upload(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Only JPEG, PNG, GIF, and WebP images are allowed."
            );
        }

        try {
            String originalFilename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().replaceAll("\\s+", "_")
                    : "file";
            String fileName = UUID.randomUUID() + "_" + originalFilename;

            bucket.create(
                    fileName,
                    file.getInputStream(),
                    contentType,
                    Bucket.BlobWriteOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ)
            );

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            return String.format(
                    "https://storage.googleapis.com/%s/%s",
                    bucket.getName(),
                    encodedFileName
            );
        } catch (IOException e) {
            throw new FileUploadException("Image upload failed.", e);
        }
    }

    public void delete(String fileUrl) {
        String baseUrl = "https://storage.googleapis.com/" + bucket.getName() + "/";
        if (!fileUrl.startsWith(baseUrl)) {
            throw new IllegalArgumentException("Invalid file URL");
        }
        String encodedFileName = fileUrl.substring(baseUrl.length());
        String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
        Blob blob = bucket.get(fileName);
        if (blob != null) {
            blob.delete();
        }
    }
}
