package com.entri.modules.media.controller;

import com.entri.common.exception.FileUploadException;
import com.entri.modules.media.service.FirebaseStorageService;
import com.entri.modules.media.dto.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class MediaController {

    private final FirebaseStorageService firebaseStorageService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<MediaUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = firebaseStorageService.upload(file);
            return ResponseEntity.ok(new MediaUploadResponse(url));
        } catch (IOException e) {
            throw new FileUploadException("Image upload failed", e);
        }
    }

    @DeleteMapping("/upload")
    public ResponseEntity<Void> delete(@RequestParam("url") String fileUrl) {
        firebaseStorageService.delete(fileUrl);
        return ResponseEntity.noContent().build();
    }
}
