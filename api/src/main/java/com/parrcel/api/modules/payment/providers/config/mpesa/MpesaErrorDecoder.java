package com.parrcel.api.modules.payment.providers.config.mpesa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parrcel.api.common.exception.PaymentProviderException;
import com.parrcel.api.modules.payment.providers.dto.mpesa.MpesaErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class MpesaErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Mpesa API error: status={}, method={}", response.status(), methodKey);

        try (InputStream body = response.body().asInputStream()) {
            MpesaErrorResponse error = objectMapper.readValue(body, MpesaErrorResponse.class);
            log.error("Mpesa error: code={}, message={}", error.errorCode(), error.errorMessage());
            return new PaymentProviderException("Mpesa [%s]: %s".formatted(error.errorCode(), error.errorMessage()));
        } catch (IOException e) {
            log.error("Failed to parse Mpesa error response", e);
        }

        return switch (response.status()) {
            case 400 -> new PaymentProviderException("Mpesa: bad request");
            case 401 -> new PaymentProviderException("Mpesa: authentication failed — check your consumer key/secret");
            case 403 -> new PaymentProviderException("Mpesa: access forbidden");
            case 404 -> new PaymentProviderException("Mpesa: endpoint not found");
            case 500 -> new PaymentProviderException("Mpesa: internal server error");
            default  -> new PaymentProviderException("Mpesa: unexpected error " + response.status());
        };
    }
}
