package com.entri.modules.media.service;

import com.entri.media.exception.FileUploadException;
import com.entri.media.exception.InvalidFileTypeException;
import com.entri.media.service.MediaService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock Bucket bucket;
    @Mock Blob blob;

    @InjectMocks MediaService mediaService;

    @Test
    void upload_disallowedContentType_throwsInvalidFileTypeException() {
        var file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", "content".getBytes());

        assertThatThrownBy(() -> mediaService.upload(file))
                .isInstanceOf(InvalidFileTypeException.class);
    }

    @Test
    void upload_missingContentType_throwsInvalidFileTypeException() {
        var file = new MockMultipartFile("file", "photo.jpg", null, "content".getBytes());

        assertThatThrownBy(() -> mediaService.upload(file))
                .isInstanceOf(InvalidFileTypeException.class);
    }

    @Test
    void upload_validImage_returnsPublicUrl() {
        var file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        when(bucket.getName()).thenReturn("my-bucket");

        var url = mediaService.upload(file);

        assertThat(url).startsWith("https://storage.googleapis.com/my-bucket/");
        assertThat(url).contains("photo.jpg");
    }

    @Test
    void upload_filenameWithSpaces_replacesSpacesWithUnderscores() {
        var file = new MockMultipartFile("file", "my photo.png", "image/png", "content".getBytes());
        when(bucket.getName()).thenReturn("my-bucket");

        var url = mediaService.upload(file);

        assertThat(url).contains("my_photo.png");
        assertThat(url).doesNotContain(" ");
    }

    @Test
    void upload_ioExceptionDuringRead_throwsFileUploadException() throws IOException {
        var file = new org.springframework.web.multipart.MultipartFile() {
            @Override public String getName() { return "file"; }
            @Override public String getOriginalFilename() { return "photo.jpg"; }
            @Override public String getContentType() { return "image/jpeg"; }
            @Override public boolean isEmpty() { return false; }
            @Override public long getSize() { return 10; }
            @Override public byte[] getBytes() { return new byte[0]; }
            @Override public InputStream getInputStream() throws IOException { throw new IOException("boom"); }
            @Override public void transferTo(java.io.File dest) { }
        };

        assertThatThrownBy(() -> mediaService.upload(file))
                .isInstanceOf(FileUploadException.class)
                .hasMessage("Image upload failed.");
    }

    @Test
    void delete_urlNotMatchingBucket_throwsIllegalArgumentException() {
        when(bucket.getName()).thenReturn("my-bucket");

        assertThatThrownBy(() -> mediaService.delete("https://storage.googleapis.com/other-bucket/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_blobExists_deletesBlob() {
        when(bucket.getName()).thenReturn("my-bucket");
        when(bucket.get("file.jpg")).thenReturn(blob);

        mediaService.delete("https://storage.googleapis.com/my-bucket/file.jpg");

        verify(blob).delete();
    }

    @Test
    void delete_blobDoesNotExist_doesNothing() {
        when(bucket.getName()).thenReturn("my-bucket");
        when(bucket.get("file.jpg")).thenReturn(null);

        mediaService.delete("https://storage.googleapis.com/my-bucket/file.jpg");

        verify(blob, never()).delete();
    }
}
