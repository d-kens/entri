package com.entri.modules.media.controller;

import com.entri.security.UserPrincipal;
import com.entri.users.entity.User;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.media.controller.MediaController;
import com.entri.media.exception.FileUploadException;
import com.entri.media.exception.InvalidFileTypeException;
import com.entri.media.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var principal = new UserPrincipal(User.builder().externalKey("owner-key").build());
        mockMvc = MockMvcBuilders.standaloneSetup(new MediaController(mediaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(UserPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return principal;
                    }
                })
                .build();
    }

    @Test
    void upload_validFile_returnsUrl() throws Exception {
        var file = new MockMultipartFile("file", "poster.png", "image/png", "content".getBytes());
        when(mediaService.upload(any(), any())).thenReturn("https://storage.example.com/poster.png");

        mockMvc.perform(multipart("/media/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://storage.example.com/poster.png"));
    }

    @Test
    void upload_disallowedFileType_returnsBadRequest() throws Exception {
        var file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", "content".getBytes());
        when(mediaService.upload(any(), any())).thenThrow(new InvalidFileTypeException("File type not allowed"));

        mockMvc.perform(multipart("/media/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_serviceThrowsIOFailure_returnsInternalServerError() throws Exception {
        var file = new MockMultipartFile("file", "poster.png", "image/png", "content".getBytes());
        when(mediaService.upload(any(), any())).thenThrow(new FileUploadException("Failed to upload file", new RuntimeException("boom")));

        mockMvc.perform(multipart("/media/upload").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void delete_validUrl_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/media").param("url", "https://storage.example.com/poster.png"))
                .andExpect(status().isNoContent());
    }
}
