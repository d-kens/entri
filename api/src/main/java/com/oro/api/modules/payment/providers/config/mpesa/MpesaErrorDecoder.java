package com.oro.api.modules.payment.providers.config.mpesa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oro.api.modules.payment.exception.PaymentProviderException;
import com.oro.api.modules.payment.providers.dto.mpesa.MpesaErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MpesaErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_BODY_BYTES = 4096;

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Mpesa API error: status={}, method={}", response.status(), methodKey);

        String rawBody = readBody(response);

        if (!rawBody.isBlank()) {
            log.error("Mpesa error raw response: {}", rawBody);

            if (rawBody.trim().startsWith("{")) {
                try {
                    MpesaErrorResponse error = objectMapper.readValue(rawBody, MpesaErrorResponse.class);
                    String code    = error.errorCode()    != null ? error.errorCode()    : "UNKNOWN";
                    String message = error.errorMessage() != null ? error.errorMessage() : "No message provided";
                    log.error("Mpesa error: code={}, message={}", code, message);
                    return new PaymentProviderException("Mpesa [%s]: %s".formatted(code, message));
                } catch (IOException e) {
                    log.error("Mpesa response looked like JSON but failed to parse: {}", rawBody, e);
                }
            }
        }

        return switch (response.status()) {
            case 400 -> new PaymentProviderException("Mpesa: bad request");
            case 401 -> new PaymentProviderException("Mpesa: authentication failed — check your consumer key/secret");
            case 403 -> new PaymentProviderException("Mpesa: access forbidden");
            case 404 -> new PaymentProviderException("Mpesa: endpoint not found");
            case 500 -> new PaymentProviderException("Mpesa: internal server error");
            case 503 -> new PaymentProviderException("Mpesa: service unavailable — Daraja API is down or unreachable");
            default  -> new PaymentProviderException("Mpesa: unexpected error " + response.status());
        };
    }

    private String readBody(Response response) {
        if (response.body() == null) return "";
        try (InputStream stream = response.body().asInputStream()) {
            byte[] bytes = stream.readNBytes(MAX_BODY_BYTES);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read Mpesa error response body", e);
            return "";
        }
    }
}