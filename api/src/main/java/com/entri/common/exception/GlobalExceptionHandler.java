package com.entri.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileUploadException.class)
    public ProblemDetail handleFileUploadException(
            final FileUploadException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceededException(
            final MaxUploadSizeExceededException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File Too Large",
                "The uploaded file exceeds the maximum allowed size.",
                request
        );
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ProblemDetail handleInvalidFileTypeException(
            final InvalidFileTypeException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid File Upload Type",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more fields contain invalid values.",
                request
        );

        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableMessage(
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid Request Body",
                "The request body is malformed or contains invalid JSON.",
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(
            final ResourceNotFoundException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InsufficientTicketsException.class)
    public ProblemDetail handleInsufficientTicketsException(
            final InsufficientTicketsException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Insufficient Tickets",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(EventNotOnSaleException.class)
    public ProblemDetail handleEventNotOnSaleException(
            final EventNotOnSaleException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Event Tickets Not On Sale",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleTicketTypeNotForEventException(
            final BadRequestException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid Ticket Type",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MaxTicketsPerOrderExceededException.class)
    public ProblemDetail handleMaxTicketsPerOrderExceededException(
            final MaxTicketsPerOrderExceededException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Max Tickets Per Order Exceeded",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidReservationStatusException.class)
    public ProblemDetail handleInvalidReservationStatusException(
            final InvalidReservationStatusException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Reservation Not Available",
                exception.getMessage(),
                request
        );
    }


    @ExceptionHandler(EmailAlreadyExist.class)
    public ProblemDetail handleEmailAlreadyExist(
            final EmailAlreadyExist exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Email Already Exist",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(
            final UnauthorizedException exception,
            final HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                "Unauthorized",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(PaymentProviderException.class)
    public ProblemDetail handlePaymentProviderException(
            final PaymentProviderException exception,
            final HttpServletRequest request
    ) {
        log.error("Payment provider error: {}", exception.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_GATEWAY,
                "Payment Provider Error",
                "An error occurred while processing your payment. Please try again later.",
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(
            final AuthenticationException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", exception.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(
            final AccessDeniedException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(HttpStatus.FORBIDDEN, "Forbidden", exception.getMessage(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(
            final HttpRequestMethodNotSupportedException exception,
            final HttpServletRequest request
    ) {
        return createProblemDetail(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                exception.getMessage(),
                request
        );
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }
}