package com.parrcel.api.modules.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenValidationDto {

    @NotBlank(message = "Token cannot be blank")
    @JsonProperty("token")
    private String token;
}
