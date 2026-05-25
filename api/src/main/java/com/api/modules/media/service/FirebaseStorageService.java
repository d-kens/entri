package com.api.modules.media.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

    private final Bucket bucket;

    public String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("\\s+", "_")
                : "file";
        String fileName = UUID.randomUUID() + "_" + originalFilename;
        Blob blob = bucket.create(fileName, file.getInputStream(), file.getContentType());
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), encodedFileName);
    }

    public void delete(String fileUrl) {
        String encodedFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
        Blob blob = bucket.get(fileName);
        if (blob != null) {
            blob.delete();
        }
    }
}
