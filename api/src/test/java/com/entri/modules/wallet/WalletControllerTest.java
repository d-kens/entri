package com.entri.modules.wallet;

import com.entri.common.dto.PaginationResponse;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.ResourceNotFoundException;
import com.entri.payment.enums.AccountType;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.wallet.WalletController;
import com.entri.wallet.WalletService;
import com.entri.wallet.WalletTransactionStatus;
import com.entri.wallet.dto.WalletResponse;
import com.entri.wallet.dto.WalletTransactionResponse;
import com.entri.wallet.dto.WithdrawalRequest;
import com.entri.wallet.dto.WithdrawalResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserPrincipal currentUser;

    private User userWithRole(Role role, String externalKey) {
        return User.builder().externalKey(externalKey).role(role).email("user@example.com").build();
    }

    private void setUpMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WalletController(walletService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(), new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                                && UserPrincipal.class.isAssignableFrom(parameter.getParameterType());
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return currentUser;
                    }
                })
                .build();
    }

    @Test
    void getPlatformWallet_returnsWallet() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ADMIN, "admin-1"));
        setUpMockMvc();
        when(walletService.getPlatformWallet()).thenReturn(new WalletResponse("wallet-platform", BigDecimal.TEN));

        mockMvc.perform(get("/wallet/platform"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("wallet-platform"))
                .andExpect(jsonPath("$.balance").value(10));
    }

    @Test
    void getWallet_ownOrganizerKey_returnsWallet() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();
        when(walletService.getWallet("org-1")).thenReturn(new WalletResponse("wallet-1", BigDecimal.valueOf(500)));

        mockMvc.perform(get("/wallet/org-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("wallet-1"));
    }

    @Test
    void getWallet_differentOrganizerKey_returnsForbidden() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();

        mockMvc.perform(get("/wallet/org-2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getWallet_adminRequestingAnyOrganizer_returnsWallet() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ADMIN, "admin-1"));
        setUpMockMvc();
        when(walletService.getWallet("org-2")).thenReturn(new WalletResponse("wallet-2", BigDecimal.ONE));

        mockMvc.perform(get("/wallet/org-2"))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactions_ownOrganizerKey_returnsPaginatedList() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();
        var transaction = new WalletTransactionResponse("txn-1", com.entri.wallet.WalletTransactionType.CREDIT,
                BigDecimal.TEN, "KES", "ref-1", WalletTransactionStatus.COMPLETED, java.time.Instant.now());
        when(walletService.getTransactions(eq("org-1"), any()))
                .thenReturn(new PaginationResponse<>(List.of(transaction), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/wallet/org-1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalId").value("txn-1"));
    }

    @Test
    void getTransactions_differentOrganizerKey_returnsForbidden() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();

        mockMvc.perform(get("/wallet/org-2/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void withdraw_validRequest_returnsWithdrawalResponse() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "123456", "Jane Doe", "Payout", null, null);
        when(walletService.withdraw(eq("wallet-1"), any(WithdrawalRequest.class), eq(currentUser)))
                .thenReturn(new WithdrawalResponse("withdrawal-1", WalletTransactionStatus.PENDING));

        mockMvc.perform(post("/wallet/wallet-1/withdraw")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("withdrawal-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void withdraw_amountBelowMinimum_returnsBadRequest() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();
        var request = new WithdrawalRequest(BigDecimal.valueOf(50), AccountType.PAYBILL, "123456", "Jane Doe", "Payout", null, null);

        mockMvc.perform(post("/wallet/wallet-1/withdraw")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_walletNotFound_returnsNotFound() throws Exception {
        currentUser = new UserPrincipal(userWithRole(Role.ORGANIZER, "org-1"));
        setUpMockMvc();
        var request = new WithdrawalRequest(BigDecimal.valueOf(500), AccountType.PAYBILL, "123456", "Jane Doe", "Payout", null, null);
        when(walletService.withdraw(eq("missing"), any(WithdrawalRequest.class), eq(currentUser)))
                .thenThrow(new ResourceNotFoundException("Wallet not found"));

        mockMvc.perform(post("/wallet/missing/withdraw")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
