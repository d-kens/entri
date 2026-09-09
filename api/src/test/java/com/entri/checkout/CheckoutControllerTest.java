package com.entri.checkout;

import com.entri.checkout.dto.CheckoutDetails;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.ResourceNotFoundException;
import com.entri.events.exception.InvalidReservationStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CheckoutController(checkoutService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private CheckoutDetails validDetails() {
        return new CheckoutDetails("jane@example.com", "Doe", "Jane", "0712345678");
    }

    @Test
    void checkout_validRequest_returnsCheckoutUrl() throws Exception {
        when(checkoutService.checkout(eq("res-1"), any(CheckoutDetails.class)))
                .thenReturn(new CheckoutResponse("https://pay.example.com/session/1"));

        mockMvc.perform(post("/checkout/res-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validDetails())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("https://pay.example.com/session/1"));
    }

    @Test
    void checkout_blankEmail_returnsBadRequest() throws Exception {
        var invalid = new CheckoutDetails("", "Doe", "Jane", "0712345678");

        mockMvc.perform(post("/checkout/res-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_invalidEmailFormat_returnsBadRequest() throws Exception {
        var invalid = new CheckoutDetails("not-an-email", "Doe", "Jane", "0712345678");

        mockMvc.perform(post("/checkout/res-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_reservationNotFound_returnsNotFound() throws Exception {
        when(checkoutService.checkout(eq("missing"), any(CheckoutDetails.class)))
                .thenThrow(new ResourceNotFoundException("Reservation not found"));

        mockMvc.perform(post("/checkout/missing")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validDetails())))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkout_reservationNotPending_returnsConflict() throws Exception {
        when(checkoutService.checkout(eq("res-1"), any(CheckoutDetails.class)))
                .thenThrow(new InvalidReservationStatusException("Reservation with ID res-1 is not available for payment"));

        mockMvc.perform(post("/checkout/res-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validDetails())))
                .andExpect(status().isConflict());
    }
}
