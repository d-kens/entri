package com.entri.modules.exception;

import com.entri.events.exception.EventNotOnSaleException;
import com.entri.events.exception.InsufficientTicketsException;
import com.entri.events.exception.InvalidReservationStatusException;
import com.entri.events.exception.MaxTicketsPerOrderExceededException;
import com.entri.exception.BadRequestException;
import com.entri.exception.ForbiddenException;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.PaymentGatewayException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.media.exception.FileUploadException;
import com.entri.media.exception.InvalidFileTypeException;
import com.entri.users.exception.EmailAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock HttpServletRequest request;

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/resource");
    }

    @Test
    void handleFileUploadException_returnsInternalServerError() {
        var exception = new FileUploadException("upload failed", new RuntimeException("cause"));

        ProblemDetail result = handler.handleFileUploadException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getDetail()).isEqualTo("upload failed");
        assertThat(result.getInstance()).hasToString("/api/resource");
    }

    @Test
    void handleMaxUploadSizeExceededException_returnsPayloadTooLarge() {
        var exception = new MaxUploadSizeExceededException(1024L);

        ProblemDetail result = handler.handleMaxUploadSizeExceededException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE.value());
        assertThat(result.getTitle()).isEqualTo("File Too Large");
        assertThat(result.getDetail()).isEqualTo("The uploaded file exceeds the maximum allowed size.");
    }

    @Test
    void handleInvalidFileTypeException_returnsBadRequest() {
        var exception = new InvalidFileTypeException("only images allowed");

        ProblemDetail result = handler.handleInvalidFileTypeException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid File Upload Type");
        assertThat(result.getDetail()).isEqualTo("only images allowed");
    }

    @Test
    void handleValidationErrors_returnsBadRequestWithFieldErrors() {
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "email", "must not be blank")
        ));
        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail result = handler.handleValidationErrors(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Validation Failed");
        assertThat(result.getDetail()).isEqualTo("One or more fields contain invalid values.");
        assertThat(result.getProperties()).containsKey("errors");
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) result.getProperties().get("errors");
        assertThat(errors).containsEntry("email", "must not be blank");
    }

    @Test
    void handleUnreadableMessage_returnsBadRequest() {
        ProblemDetail result = handler.handleUnreadableMessage(request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid Request Body");
        assertThat(result.getDetail()).isEqualTo("The request body is malformed or contains invalid JSON.");
    }

    @Test
    void handleResourceNotFoundException_returnsNotFound() {
        var exception = new ResourceNotFoundException("user not found");

        ProblemDetail result = handler.handleResourceNotFoundException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Resource Not Found");
        assertThat(result.getDetail()).isEqualTo("user not found");
    }

    @Test
    void handleInsufficientTicketsException_returnsConflict() {
        var exception = new InsufficientTicketsException("not enough tickets");

        ProblemDetail result = handler.handleInsufficientTicketsException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Insufficient Tickets");
        assertThat(result.getDetail()).isEqualTo("not enough tickets");
    }

    @Test
    void handleEventNotOnSaleException_returnsConflict() {
        var exception = new EventNotOnSaleException("event not on sale");

        ProblemDetail result = handler.handleEventNotOnSaleException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Event Tickets Not On Sale");
        assertThat(result.getDetail()).isEqualTo("event not on sale");
    }

    @Test
    void handleBadRequestException_returnsBadRequest() {
        var exception = new BadRequestException("bad input");

        ProblemDetail result = handler.handleBadRequestException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid Request");
        assertThat(result.getDetail()).isEqualTo("bad input");
    }

    @Test
    void handleMaxTicketsPerOrderExceededException_returnsBadRequest() {
        var exception = new MaxTicketsPerOrderExceededException("too many tickets");

        ProblemDetail result = handler.handleMaxTicketsPerOrderExceededException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Max Tickets Per Order Exceeded");
        assertThat(result.getDetail()).isEqualTo("too many tickets");
    }

    @Test
    void handleInvalidReservationStatusException_returnsConflict() {
        var exception = new InvalidReservationStatusException("reservation expired");

        ProblemDetail result = handler.handleInvalidReservationStatusException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Reservation Not Available");
        assertThat(result.getDetail()).isEqualTo("reservation expired");
    }

    @Test
    void handleEmailAlreadyExistsException_returnsConflict() {
        var exception = new EmailAlreadyExistsException();

        ProblemDetail result = handler.handleEmailAlreadyExistsException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("Email Already Exist");
        assertThat(result.getDetail()).isEqualTo("Email already exist");
    }

    @Test
    void handleUnauthorizedException_returnsUnauthorized() {
        var exception = new UnauthorizedException("invalid token");

        ProblemDetail result = handler.handleUnauthorizedException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("Unauthorized");
        assertThat(result.getDetail()).isEqualTo("invalid token");
    }

    @Test
    void handleForbiddenException_returnsForbidden() {
        var exception = new ForbiddenException("not allowed");

        ProblemDetail result = handler.handleForbiddenException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("Forbidden");
        assertThat(result.getDetail()).isEqualTo("not allowed");
    }

    @Test
    void handlePaymentProviderException_returnsBadGateway() {
        var exception = new PaymentGatewayException("gateway timeout");

        ProblemDetail result = handler.handlePaymentProviderException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(result.getTitle()).isEqualTo("Payment Provider Error");
        assertThat(result.getDetail()).isEqualTo("An error occurred while processing your payment. Please try again later.");
    }

    @Test
    void handleAuthenticationException_returnsUnauthorized() {
        var exception = new BadCredentialsException("bad credentials");

        ProblemDetail result = handler.handleAuthenticationException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("Unauthorized");
        assertThat(result.getDetail()).isEqualTo("bad credentials");
    }

    @Test
    void handleAccessDeniedException_returnsForbidden() {
        var exception = new AccessDeniedException("access denied");

        ProblemDetail result = handler.handleAccessDeniedException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("Forbidden");
        assertThat(result.getDetail()).isEqualTo("access denied");
    }

    @Test
    void handleMethodNotAllowed_returnsMethodNotAllowed() {
        var exception = new HttpRequestMethodNotSupportedException("POST");

        ProblemDetail result = handler.handleMethodNotAllowed(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(result.getTitle()).isEqualTo("Method Not Allowed");
        assertThat(result.getDetail()).isEqualTo(exception.getMessage());
    }

    @Test
    void handleUnexpectedException_returnsInternalServerError() {
        var exception = new RuntimeException("boom");

        ProblemDetail result = handler.handleUnexpectedException(exception, request);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}
