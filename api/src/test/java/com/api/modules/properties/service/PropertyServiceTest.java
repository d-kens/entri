package com.api.modules.properties.service;

import com.api.common.exception.FileUploadException;
import com.api.common.storage.FirebaseStorageService;
import com.api.modules.properties.dto.CreatePropertyRequest;
import com.api.modules.properties.dto.PropertyResponse;
import com.api.modules.properties.entity.Property;
import com.api.modules.properties.repository.PropertyRepository;
import com.api.modules.properties.service.mapper.PropertyMapper;
import com.api.modules.users.entity.User;
import com.api.modules.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock UserService userService;
    @Mock PropertyMapper propertyMapper;
    @Mock PropertyRepository propertyRepository;
    @Mock FirebaseStorageService firebaseStorageService;

    @InjectMocks PropertyService propertyService;

    @Test
    void createProperty_savesPropertyAndReturnsResponse() throws IOException {
        var coverImage = mock(MultipartFile.class);
        var request = new CreatePropertyRequest(
                "Sunset Apartments", "Nice place", 5,
                "Kenya", "Nairobi", "Westlands",
                new BigDecimal("1.2921"), new BigDecimal("36.8219"),
                coverImage, true
        );
        var user = User.builder().build();
        var expectedResponse = new PropertyResponse(
                "ext-key", "Sunset Apartments", "Nice place", 5,
                "Kenya", "Nairobi", "Westlands",
                new BigDecimal("1.2921"), new BigDecimal("36.8219"),
                "https://storage.googleapis.com/bucket/img.jpg", true
        );

        when(userService.findEntityByExternalKey("ext-key")).thenReturn(user);
        when(firebaseStorageService.upload(coverImage)).thenReturn("https://storage.googleapis.com/bucket/img.jpg");
        when(propertyMapper.toPropertyresponse(any(Property.class))).thenReturn(expectedResponse);

        var result = propertyService.createProperty(request, "ext-key");

        assertThat(result).isEqualTo(expectedResponse);
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void createProperty_imageUploadFails_throwsFileUploadException() throws IOException {
        var coverImage = mock(MultipartFile.class);
        var request = new CreatePropertyRequest(
                "Sunset Apartments", "Nice place", 5,
                "Kenya", "Nairobi", "Westlands",
                new BigDecimal("1.2921"), new BigDecimal("36.8219"),
                coverImage, true
        );

        when(userService.findEntityByExternalKey("ext-key")).thenReturn(User.builder().build());
        when(firebaseStorageService.upload(coverImage)).thenThrow(new IOException("network error"));

        assertThatThrownBy(() -> propertyService.createProperty(request, "ext-key"))
                .isInstanceOf(FileUploadException.class)
                .hasMessage("Failed to upload cover image");

        verify(propertyRepository, never()).save(any());
    }
}
