package com.entri.modules.file_upload.controller;

import com.entri.common.dto.AuthenticatedUser;
import com.entri.modules.file_upload.dto.MediaUploadResponse;
import com.entri.modules.file_upload.service.FileUploadService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@Validated
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<MediaUploadResponse> upload(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("file") MultipartFile file) {

        String url = fileUploadService.upload(file);
        return ResponseEntity.ok(new MediaUploadResponse(url));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @NotBlank @RequestParam("url") String fileUrl) {
        fileUploadService.delete(fileUrl);
        return ResponseEntity.noContent().build();
    }
}
