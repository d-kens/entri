package com.entri.media.controller.api;

import com.entri.media.dto.MediaUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/media")
public interface MediaApi {

    @Operation(
            operationId = "uploadMedia",
            summary = "Upload Media",
            description = "Uploads a media file and returns the publicly accessible URL. Accepted content type is multipart/form-data"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid or the file is missing",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "File uploaded successfully"
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    ResponseEntity<MediaUploadResponse> upload(
            @Parameter(description = "The file to upload", required = true)
            @RequestParam("file") MultipartFile file
    );

    @Operation(
            operationId = "deleteMedia",
            summary = "Delete Media",
            description = "Deletes the media file at the specified URL. On success, the API returns no content"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The URL parameter is missing or blank",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "File deleted successfully"
            )
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @DeleteMapping
    ResponseEntity<Void> delete(
            @Parameter(description = "The URL of the file to delete", required = true)
            @NotBlank @RequestParam("url") String fileUrl
    );
}
