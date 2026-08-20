package com.entri.media.controller;

import com.entri.media.controller.api.MediaApi;
import com.entri.media.dto.MediaUploadResponse;
import com.entri.media.service.MediaService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Validated
public class MediaController implements MediaApi {

    private final MediaService fileUploadService;

    @Override
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file) {

        String url = fileUploadService.upload(file);
        return ResponseEntity.ok(new MediaUploadResponse(url));
    }

    @Override
    public ResponseEntity<Void> delete(
            @NotBlank @RequestParam("url") String fileUrl) {
        fileUploadService.delete(fileUrl);
        return ResponseEntity.noContent().build();
    }
}
