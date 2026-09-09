package com.entri.modules.payment;

import com.entri.payment.PaymentController;
import com.entri.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService)).build();
    }

    @Test
    void handleWebhook_delegatesPayloadAndHeadersToService_returnsOk() throws Exception {
        String payload = "{\"event\":\"payment.completed\"}";

        mockMvc.perform(post("/payment/webhook")
                        .header("X-IntaSend-Signature", "sig-123")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());

        ArgumentCaptor<Map<String, String>> headersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(paymentService).handleWebhook(headersCaptor.capture(), eq(payload));
        assertThat(headersCaptor.getValue()).containsEntry("X-IntaSend-Signature", "sig-123");
    }

    @Test
    void handleWebhook_emptyBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/payment/webhook")
                        .contentType("application/json")
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
